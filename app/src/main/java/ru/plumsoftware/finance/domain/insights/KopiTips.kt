package ru.plumsoftware.finance.domain.insights

import ru.plumsoftware.finance.domain.budget.DailyBudget

/** Совет Коппи (ТЗ §8.6). Тексты формирует UI. */
sealed interface KopiTip {
    /** Сегодня лимит превышен на [over]. Если завтра уложиться в [tomorrow], месяц сойдётся. */
    data class DailyExceeded(val over: Long, val tomorrow: Long) : KopiTip

    /** [category] в этом месяце на [percent]% дороже прошлого. */
    data class CategoryGrowth(val category: String, val percent: Int) : KopiTip

    /** Завтра спишется [title] — [amount]. */
    data class ChargeTomorrow(val title: String, val amount: Long) : KopiTip

    /** При текущем темпе перерасход ~[over]. Держаться [perDay] в день. */
    data class ForecastOverspend(val over: Long, val perDay: Long) : KopiTip

    /** [asset] окупилась. */
    data class AssetPaidOff(val asset: String) : KopiTip

    /** Нейтральный совет из списка. */
    data class Neutral(val index: Int) : KopiTip
}

object KopiTips {
    const val NEUTRAL_COUNT = 10
    private const val GROWTH_THRESHOLD = 15
    private const val CHARGE_THRESHOLD_MINOR = 100_000L

    data class CategoryMonthSpend(val name: String, val current: Long, val previous: Long)
    data class UpcomingCharge(val title: String, val amount: Long, val daysUntil: Int)

    /** Выбор совета строго по приоритету §8.6. */
    fun pick(
        budget: DailyBudget?,
        categories: List<CategoryMonthSpend>,
        upcoming: List<UpcomingCharge>,
        paidOffAssets: List<String>,
        epochDay: Long,
        lastNeutralIndex: Int,
        lastNeutralEpochDay: Long,
    ): KopiTip {
        // 1. Превышен дневной лимит.
        if (budget != null && budget.budget > 0 && budget.left < 0) {
            return KopiTip.DailyExceeded(over = -budget.left, tomorrow = perDayAfterToday(budget).coerceAtLeast(0))
        }
        // 2. Категория выросла > 15% к прошлому месяцу.
        categories
            .filter { it.previous > 0 && it.current > it.previous }
            .map { it to ((it.current - it.previous) * 100 / it.previous).toInt() }
            .filter { it.second > GROWTH_THRESHOLD }
            .maxByOrNull { it.first.current - it.first.previous }
            ?.let { return KopiTip.CategoryGrowth(it.first.name, it.second) }
        // 3. Завтра списание > 1 000 ₽.
        upcoming.filter { it.daysUntil == 1 && it.amount > CHARGE_THRESHOLD_MINOR }
            .maxByOrNull { it.amount }
            ?.let { return KopiTip.ChargeTomorrow(it.title, it.amount) }
        // 4. Прогноз с перерасходом.
        if (budget != null && budget.budget > 0 && budget.diff < 0) {
            return KopiTip.ForecastOverspend(over = -budget.diff, perDay = perDayAfterToday(budget).coerceAtLeast(0))
        }
        // 5. Актив умной экономии окупился.
        paidOffAssets.firstOrNull()?.let { return KopiTip.AssetPaidOff(it) }
        // 6. Нейтральный совет: в течение дня тот же, на следующий день — следующий по кругу,
        // поэтому каждый повторяется не чаще раза в NEUTRAL_COUNT дней (≥ недели).
        val index = when {
            lastNeutralIndex < 0 -> (epochDay % NEUTRAL_COUNT).toInt()
            lastNeutralEpochDay == epochDay -> lastNeutralIndex
            else -> (lastNeutralIndex + 1) % NEUTRAL_COUNT
        }
        return KopiTip.Neutral(index)
    }

    /** Сколько тратить в день до конца месяца, начиная с завтра. */
    fun perDayAfterToday(b: DailyBudget): Long {
        val remainingDays = b.daysLeft - 1
        val remaining = b.budget - b.spentMonth
        return if (remainingDays <= 0) remaining else remaining / remainingDays
    }
}
