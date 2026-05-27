package ru.plumsoftware.finance.domain.model

data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amountMinor: Long,
    val categoryId: Long?,
    val smartAssetId: Long?,
    val note: String?,
    val dateMillis: Long,
    val createdAtMillis: Long,
)
