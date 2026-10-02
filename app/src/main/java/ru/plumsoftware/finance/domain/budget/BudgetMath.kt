package ru.plumsoftware.finance.domain.budget

import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToLong

/** Результат расчёта дневного лимита и прогноза (ТЗ §8.1). Все суммы в копейках. */
data class DailyBudget(
    val budget: Long,
    val spentBefore: Long,
    val spentToday: Long,
    val spentMonth: Long,
    val daysLeft: Int,
    val dayOfMonth: Int,
    val daysInMonth: Int,
    /** Дневная норма на сегодня: (budget − spentBefore) / daysLeft. */
    val daily: Long,
    /** Сколько ещё можно потратить сегодня; может быть < 0. */
    val left: Long,
    val forecast: Long,
    /** budget − forecast: ≥ 0 «уложитесь», < 0 «перерасход». */
    val diff: Long,
    /** budget − spentBefore ≤ 0. */
    val exhausted: Boolean,
) {
    /** Доля потраченного сегодня от дневной нормы (для прогресс-бара hero). */
    val todayRatio: Float
        get() = when {
            daily <= 0L -> if (spentToday > 0) 2f else 0f
            else -> spentToday.toFloat() / daily
        }

    /** Перерасход бюджета месяца при исчерпании. */
    val monthOverspend: Long get() = (spentMonth - budget).coerceAtLeast(0L)
}

object BudgetMath {

    /** Округление до 100 ₽ (10 000 копеек). */
    fun roundTo100Rub(minor: Double): Long = (minor / 10_000.0).roundToLong() * 10_000L

    /**
     * §8.1. [expensesByDay] — расходы по дням текущего месяца (ключ — число месяца).
     */
    fun compute(budget: Long, expensesByDay: Map<Int, Long>, today: LocalDate): DailyBudget {
        val ym = YearMonth.from(today)
        val daysInMonth = ym.lengthOfMonth()
        val dayOfMonth = today.dayOfMonth
        val daysLeft = daysInMonth - dayOfMonth + 1
        val spentBefore = expensesByDay.filterKeys { it < dayOfMonth }.values.sum()
        val spentToday = expensesByDay[dayOfMonth] ?: 0L
        val remainingBudget = budget - spentBefore
        val daily = if (daysLeft > 0) remainingBudget / daysLeft else remainingBudget
        val left = daily - spentToday
        val forecast = roundTo100Rub((spentBefore + spentToday).toDouble() / dayOfMonth * daysInMonth)
        return DailyBudget(
            budget = budget,
            spentBefore = spentBefore,
            spentToday = spentToday,
            spentMonth = spentBefore + spentToday,
            daysLeft = daysLeft,
            dayOfMonth = dayOfMonth,
            daysInMonth = daysInMonth,
            daily = daily,
            left = left,
            forecast = forecast,
            diff = budget - forecast,
            exhausted = remainingBudget <= 0L,
        )
    }

    /**
     * Бюджет месяца (§8.1): явно заданный → сумма лимитов категорий → средние расходы за 3 месяца.
     */
    fun resolveBudget(explicit: Long?, categoryLimitsSum: Long, avgExpensesLast3Months: Long): Long = when {
        explicit != null && explicit > 0 -> explicit
        categoryLimitsSum > 0 -> categoryLimitsSum
        else -> avgExpensesLast3Months
    }

    enum class DayColor { NORMAL, OVER, TODAY, FUTURE }

    /** §8.2: цвет каждого дня месяца. */
    fun dayColors(budget: Long, expensesByDay: Map<Int, Long>, today: LocalDate): List<DayColor> {
        val daysInMonth = YearMonth.from(today).lengthOfMonth()
        val avg = budget.toDouble() / daysInMonth
        return (1..daysInMonth).map { d ->
            when {
                d < today.dayOfMonth -> if ((expensesByDay[d] ?: 0L) > avg * 1.3) DayColor.OVER else DayColor.NORMAL
                d == today.dayOfMonth -> DayColor.TODAY
                else -> DayColor.FUTURE
            }
        }
    }

    /** Предлагаемый лимит категории (§6.11 п.4): траты × 1.2, вверх до 1 000 ₽. */
    fun suggestLimit(spentMonth: Long): Long {
        val step = 100_000L
        val raw = (spentMonth * 1.2).toLong()
        if (raw <= 0) return step
        return ((raw + step - 1) / step) * step
    }

    enum class LimitStatus { OK, ALMOST, EXCEEDED }

    /** §8.4: < 80 — в норме, 80–99 — почти, ≥ 100 — превышен. */
    fun limitStatus(spent: Long, limit: Long): LimitStatus {
        if (limit <= 0) return LimitStatus.OK
        val p = spent * 100.0 / limit
        return when {
            p >= 100 -> LimitStatus.EXCEEDED
            p >= 80 -> LimitStatus.ALMOST
            else -> LimitStatus.OK
        }
    }
}
