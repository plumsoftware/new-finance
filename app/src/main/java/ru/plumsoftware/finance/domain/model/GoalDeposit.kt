package ru.plumsoftware.finance.domain.model

data class GoalDeposit(
    val id: Long = 0,
    val goalId: Long,
    val amountMinor: Long,
    val note: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
