package ru.plumsoftware.finance.domain.model

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amountMinor: Long,
    val categoryId: Long?,
    val smartAssetId: Long?,
    val goalId: Long? = null,
    val note: String?,
    val dateMillis: Long,
    val createdAtMillis: Long,
    val accountId: Long = 1L,
    val currencyCode: String = "RUB",
    val originalAmountMinor: Long = 0L,
    val originalCurrencyCode: String? = null,
    val exchangeRate: Double = 1.0,
)
