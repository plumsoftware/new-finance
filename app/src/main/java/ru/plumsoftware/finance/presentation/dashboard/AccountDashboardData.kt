package ru.plumsoftware.finance.presentation.dashboard

data class AccountDashboardData(
    val accountId: Long?,
    val name: String,
    val balanceMinor: Long,
    val currencyCode: String,
    val monthIncomeMinor: Long,
    val monthExpenseMinor: Long,
    val isSavings: Boolean
)