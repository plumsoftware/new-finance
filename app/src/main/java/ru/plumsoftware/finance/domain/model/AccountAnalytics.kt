package ru.plumsoftware.finance.domain.model

data class AccountAnalytics(
    val account: Account,
    val incomeMinor: Long,
    val expenseMinor: Long,
    val balanceMinor: Long,
    val transactionCount: Int,
    val incomeShare: Float,
    val expenseShare: Float,
)
