package ru.plumsoftware.finance.domain.model

data class ExportData(
    val periodLabel: String,
    val generatedAt: Long,
    val accounts: List<ExportAccount>,
    val transactions: List<ExportTransaction>,
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val currencyCode: String,
)

data class ExportAccount(
    val name: String,
    val type: String,
    val currencyCode: String,
    val balanceMinor: Long,
)

data class ExportTransaction(
    val date: Long,
    val type: String, // "Доход" / "Расход"
    val amountMinor: Long,
    val currencyCode: String,
    val originalAmountMinor: Long,
    val originalCurrencyCode: String?,
    val categoryName: String?,
    val accountName: String,
    val note: String?,
)