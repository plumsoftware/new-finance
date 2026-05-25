package ru.plumsoftware.finance.domain.model

data class DailySummary(
    val dateMillis: Long,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

data class CategorySpending(
    val category: Category,
    val amountMinor: Long,
    val sharePercent: Float,
)

data class PeriodSummary(
    val incomeMinor: Long,
    val expenseMinor: Long,
    val savingsMinor: Long,
    val netMinor: Long,
)

data class SavingsIndex(
    val totalSmartSavingsMinor: Long,
    val estimatedMonthlyReductionMinor: Long,
)
