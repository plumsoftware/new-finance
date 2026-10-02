package ru.plumsoftware.finance.domain.budget

import ru.plumsoftware.finance.domain.model.RecurringFrequency
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Ближайшее списание повторяющейся операции (§6.1.5). */
data class UpcomingCharge(
    val recurringId: Long,
    val title: String,
    val categoryId: Long,
    val date: LocalDate,
    val daysUntil: Int,
    val amountMinor: Long,
    val isIncome: Boolean,
)

object Upcoming {

    /** День [day] в месяце [ym], а если месяц короче — его последний день. */
    fun clampDay(ym: java.time.YearMonth, day: Int): LocalDate = ym.atDay(day.coerceIn(1, ym.lengthOfMonth()))

    /** Первое ежемесячное списание: ближайшее число [dayOfMonth], начиная с сегодня. */
    fun firstMonthlyDate(today: LocalDate, dayOfMonth: Int): LocalDate {
        val thisMonth = clampDay(java.time.YearMonth.from(today), dayOfMonth)
        return if (!thisMonth.isBefore(today)) thisMonth else clampDay(java.time.YearMonth.from(today).plusMonths(1), dayOfMonth)
    }

    /** Следующее ежемесячное списание после [date] с учётом выбранного числа. */
    fun nextMonthly(date: LocalDate, dayOfMonth: Int?): LocalDate =
        if (dayOfMonth == null) date.plusMonths(1) else clampDay(java.time.YearMonth.from(date).plusMonths(1), dayOfMonth)

    fun next(date: LocalDate, frequency: RecurringFrequency): LocalDate = when (frequency) {
        RecurringFrequency.DAILY -> date.plusDays(1)
        RecurringFrequency.WEEKLY -> date.plusWeeks(1)
        RecurringFrequency.MONTHLY -> date.plusMonths(1)
        RecurringFrequency.YEARLY -> date.plusYears(1)
    }

    /** Все списания в окне `[today, today + days]`, отсортированные по дате. */
    fun occurrences(
        items: List<RecurringTransaction>,
        today: LocalDate,
        days: Int,
        toDate: (Long) -> LocalDate,
        includeIncome: Boolean = false,
    ): List<UpcomingCharge> {
        val until = today.plusDays(days.toLong())
        val result = mutableListOf<UpcomingCharge>()
        items.filter { it.isActive && (includeIncome || !it.isIncome) }.forEach { r ->
            var d = toDate(r.nextDateMillis)
            var guard = 0
            while (d.isBefore(today) && guard++ < 1000) {
                d = if (r.frequency == RecurringFrequency.MONTHLY) nextMonthly(d, r.dayOfMonth) else next(d, r.frequency)
            }
            while (!d.isAfter(until) && guard++ < 1000) {
                result += UpcomingCharge(
                    recurringId = r.id,
                    title = r.title,
                    categoryId = r.categoryId,
                    date = d,
                    daysUntil = ChronoUnit.DAYS.between(today, d).toInt(),
                    amountMinor = r.amountMinor,
                    isIncome = r.isIncome,
                )
                d = if (r.frequency == RecurringFrequency.MONTHLY) nextMonthly(d, r.dayOfMonth) else next(d, r.frequency)
            }
        }
        return result.sortedWith(compareBy({ it.date }, { -it.amountMinor }))
    }
}
