package ru.plumsoftware.finance.domain.insights

import ru.plumsoftware.finance.domain.model.Insight
import ru.plumsoftware.finance.domain.model.InsightSeverity
import ru.plumsoftware.finance.domain.model.InsightType
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType

class InsightsEngine {

    fun generateInsights(
        currentMonth: List<Transaction>,
        previousMonth: List<Transaction>,
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        val currentExpenses = currentMonth.filter {
            it.type == TransactionType.EXPENSE && it.categoryId != null
        }
        val previousExpenses = previousMonth.filter {
            it.type == TransactionType.EXPENSE && it.categoryId != null
        }

        val currentByCategory = currentExpenses.groupBy { it.categoryId!! }
        val prevByCategory = previousExpenses.groupBy { it.categoryId!! }
        currentByCategory.forEach { (categoryId, transactions) ->
            val currentTotal = transactions.sumOf { it.amountMinor }
            val previousTotal = prevByCategory[categoryId]?.sumOf { it.amountMinor } ?: 0L
            if (previousTotal > 0L && currentTotal > previousTotal * 1.4) {
                val increasePercent = ((currentTotal - previousTotal).toDouble() / previousTotal * 100).toInt()
                insights += Insight(
                    type = InsightType.CATEGORY_SPIKE,
                    categoryId = categoryId,
                    value = increasePercent,
                    severity = if (currentTotal > previousTotal * 2) {
                        InsightSeverity.HIGH
                    } else {
                        InsightSeverity.MEDIUM
                    },
                )
            }
        }

        val income = currentMonth
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amountMinor }
        val expenses = currentMonth
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amountMinor }
        val savingsRate = if (income > 0L) {
            (income - expenses).toDouble() / income.toDouble()
        } else {
            0.0
        }
        if (savingsRate >= 0.2) {
            insights += Insight(
                type = InsightType.GOOD_SAVINGS,
                value = (savingsRate * 100).toInt(),
            )
        }

        currentExpenses.maxByOrNull { it.amountMinor }?.let { topExpense ->
            if (income > 0L && topExpense.amountMinor > income * 0.3) {
                insights += Insight(
                    type = InsightType.LARGE_EXPENSE,
                    transactionId = topExpense.id,
                    categoryId = topExpense.categoryId,
                    value = topExpense.amountMinor,
                    severity = InsightSeverity.HIGH,
                )
            }
        }

        return insights
            .sortedByDescending { it.severity.ordinal }
            .take(3)
    }
}
