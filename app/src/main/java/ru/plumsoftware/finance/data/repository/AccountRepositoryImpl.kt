package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.AccountDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.model.AccountType
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.domain.repository.AccountRepository

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
) : AccountRepository {

    override fun observeAllActive(): Flow<List<Account>> =
        accountDao.observeAllActive().map { rows -> rows.map { it.toDomain() } }

    override fun observeAllWithBalances(): Flow<List<AccountWithBalance>> =
        accountDao.observeAllWithBalances().map { rows -> rows.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Account?> =
        accountDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: Long): Account? =
        accountDao.getById(id)?.toDomain()

    override suspend fun getDefault(): Account? =
        accountDao.getDefault()?.toDomain()

    override suspend fun ensureDefaultAccount() {
        if (accountDao.getDefault() == null) {
            accountDao.insert(
                Account(
                    id = 1L,
                    name = "Основной счёт",
                    type = AccountType.DEBIT,
                    currencyCode = "RUB",
                    colorHex = "#007AFF",
                    emoji = "💳",
                    isDefault = true,
                    sortOrder = 0,
                    createdAtMillis = System.currentTimeMillis(),
                ).toEntity(),
            )
        }
    }

    override suspend fun upsert(account: Account): Long {
        if (account.id == 0L) {
            return accountDao.insert(account.toEntity())
        }
        accountDao.update(account.toEntity())
        return account.id
    }

    override suspend fun archive(id: Long) {
        val account = accountDao.getById(id) ?: return
        if (account.isDefault) return
        accountDao.archive(id)
    }

    override suspend fun delete(id: Long) {
        val account = accountDao.getById(id) ?: return
        if (account.isDefault) return
        val defaultAccount = accountDao.getDefault() ?: return
        transactionDao.moveToAccount(id, defaultAccount.id)
        accountDao.delete(id)
    }
}
