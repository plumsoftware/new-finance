package ru.plumsoftware.finance.domain.model

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currencyCode: String,
    val colorHex: String = "#007AFF",
    val emoji: String = "💳",
    val initialBalanceMinor: Long = 0,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class AccountWithBalance(
    val account: Account,
    val calculatedBalanceMinor: Long,
)
