package ru.plumsoftware.finance.domain.model

data class GoalDeposit(
    val id: Long = 0,
    val goalId: Long,
    val amountMinor: Long,
    val note: String?,
    val createdAtMillis: Long,
    val currencyCode: String = "RUB",
    val accountId: Long? = null,
)
