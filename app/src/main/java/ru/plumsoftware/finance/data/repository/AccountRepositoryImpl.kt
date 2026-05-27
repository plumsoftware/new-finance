package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.AccountDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.database.DefaultAccounts
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.AccountRepository

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
) : AccountRepository {

    override fun observeAll(): Flow<List<Account>> =
        accountDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeVisibleWithBalances(): Flow<List<AccountWithBalance>> =
        combine(
            accountDao.observeAll(),
            transactionDao.observeAll(),
        ) { accounts, transactions ->
            accounts
                .filter { !it.isHidden }
                .map { account ->
                    AccountWithBalance(
                        account = account.toDomain(),
                        balanceMinor = calculateBalance(account.initialBalanceMinor, account.id, transactions),
                    )
                }
        }

    override suspend fun getById(id: Long): Account? =
        accountDao.getById(id)?.toDomain()

    override suspend fun getBalanceMinor(accountId: Long): Long {
        val account = accountDao.getById(accountId) ?: return 0L
        val income = transactionDao.sumByTypeInPeriod(
            TransactionType.INCOME,
            0L,
            Long.MAX_VALUE,
            accountId,
        )
        val expense = transactionDao.sumByTypeInPeriod(
            TransactionType.EXPENSE,
            0L,
            Long.MAX_VALUE,
            accountId,
        )
        return account.initialBalanceMinor + income - expense
    }

    override suspend fun getTotalBalanceMinor(): Long =
        accountDao.getAll().sumOf { getBalanceMinor(it.id) }

    override suspend fun upsert(account: Account): Long {
        val entity = account.toEntity()
        return if (entity.id == 0L) accountDao.insert(entity) else {
            accountDao.update(entity)
            entity.id
        }
    }

    override suspend fun delete(id: Long) {
        accountDao.deleteById(id)
    }

    override suspend fun count(): Int = accountDao.count()

    override suspend fun ensureDefaultAccounts(currencyCode: String) {
        if (accountDao.count() == 0) {
            DefaultAccounts.initial(currencyCode).forEach { accountDao.insert(it) }
        }
    }

    override suspend fun getDefaultAccountId(currencyCode: String): Long {
        ensureDefaultAccounts(currencyCode)
        val all = accountDao.getAll().map { it.toDomain() }
        return all.filter { it.currencyCode == currencyCode }.firstOrNull()?.id
            ?: all.firstOrNull()?.id
            ?: error("No account available")
    }

    private fun calculateBalance(
        initialBalanceMinor: Long,
        accountId: Long,
        transactions: List<ru.plumsoftware.finance.data.local.entity.TransactionEntity>,
    ): Long = initialBalanceMinor + transactions
        .filter { it.accountId == accountId }
        .sumOf { tx ->
            when (tx.type) {
                TransactionType.INCOME -> tx.amountMinor
                TransactionType.EXPENSE -> -tx.amountMinor
                TransactionType.SAVINGS -> 0L
            }
        }
}
