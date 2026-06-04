package ru.plumsoftware.finance.domain.model

enum class AccountType {
    DEBIT,
    CREDIT,
    CASH,
    SAVINGS,
    OTHER,
}

fun AccountType.localizedNameRes(): Int = when (this) {
    AccountType.DEBIT -> ru.plumsoftware.finance.R.string.account_type_debit
    AccountType.CREDIT -> ru.plumsoftware.finance.R.string.account_type_credit
    AccountType.CASH -> ru.plumsoftware.finance.R.string.account_type_cash
    AccountType.SAVINGS -> ru.plumsoftware.finance.R.string.account_type_savings
    AccountType.OTHER -> ru.plumsoftware.finance.R.string.account_type_other
}
