package ru.plumsoftware.finance.domain.model

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currencyCode: String,
    val initialBalanceMinor: Long,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0,
)

data class AccountWithBalance(
    val account: Account,
    val balanceMinor: Long,
)
