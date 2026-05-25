package ru.plumsoftware.finance.data.local.database

import ru.plumsoftware.finance.data.local.entity.AccountEntity
import ru.plumsoftware.finance.domain.model.AccountType

object DefaultAccounts {
    fun initial(currencyCode: String = "RUB"): List<AccountEntity> = listOf(
        AccountEntity(
            name = "Наличные",
            type = AccountType.CASH,
            currencyCode = currencyCode,
            initialBalanceMinor = 0,
            isHidden = false,
            sortOrder = 0,
        ),
        AccountEntity(
            name = "Карта",
            type = AccountType.CARD,
            currencyCode = currencyCode,
            initialBalanceMinor = 0,
            isHidden = false,
            sortOrder = 1,
        ),
        AccountEntity(
            name = "Копилка",
            type = AccountType.PIGGY_BANK,
            currencyCode = currencyCode,
            initialBalanceMinor = 0,
            isHidden = false,
            sortOrder = 2,
        ),
    )
}
