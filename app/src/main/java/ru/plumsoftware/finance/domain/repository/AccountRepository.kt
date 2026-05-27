package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.model.AccountWithBalance

interface AccountRepository {
    fun observeAll(): Flow<List<Account>>
    fun observeVisibleWithBalances(): Flow<List<AccountWithBalance>>
    suspend fun getById(id: Long): Account?
    suspend fun getBalanceMinor(accountId: Long): Long
    suspend fun getTotalBalanceMinor(): Long
    suspend fun upsert(account: Account): Long
    suspend fun delete(id: Long)
    suspend fun count(): Int
    suspend fun ensureDefaultAccounts(currencyCode: String = "RUB")
    suspend fun getDefaultAccountId(currencyCode: String = "RUB"): Long
}
