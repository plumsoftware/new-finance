package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.model.AccountWithBalance

interface AccountRepository {
    fun observeAllActive(): Flow<List<Account>>
    fun observeAllWithBalances(): Flow<List<AccountWithBalance>>
    fun observeById(id: Long): Flow<Account?>
    suspend fun getById(id: Long): Account?
    suspend fun getDefault(): Account?
    suspend fun ensureDefaultAccount()
    suspend fun upsert(account: Account): Long
    suspend fun archive(id: Long)
    suspend fun delete(id: Long)
}
