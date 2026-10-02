package ru.plumsoftware.finance.domain.budget

import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth

/** Сборка дневного бюджета из операций (§8.1). Главная и бюджет месяца исключения Аналитики не учитывают. */
object BudgetService {

    data class Result(val budget: DailyBudget, val monthByDay: Map<Int, Long>)

    fun compute(
        transactions: List<Transaction>,
        categories: List<Category>,
        explicitBudget: Long?,
        today: LocalDate,
        toDate: (Long) -> LocalDate,
    ): Result? {
        val ym = YearMonth.from(today)
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.map { it to toDate(it.dateMillis) }
        val monthByDay = expenses.filter { YearMonth.from(it.second) == ym }
            .groupBy { it.second.dayOfMonth }
            .mapValues { e -> e.value.sumOf { it.first.amountMinor } }
        val limitsSum = categories.filter { it.type == CategoryType.EXPENSE && !it.isHidden }.sumOf { it.monthlyLimitMinor ?: 0L }
        val last3Totals = (1..3).map { ym.minusMonths(it.toLong()) }
            .map { m -> expenses.filter { YearMonth.from(it.second) == m }.sumOf { it.first.amountMinor } }
        val monthsWithData = last3Totals.count { it > 0 }
        val avg3 = if (monthsWithData == 0) 0L else last3Totals.sum() / monthsWithData
        val budget = BudgetMath.resolveBudget(explicitBudget, limitsSum, avg3)
        if (budget <= 0) return null
        return Result(BudgetMath.compute(budget, monthByDay, today), monthByDay)
    }
}
