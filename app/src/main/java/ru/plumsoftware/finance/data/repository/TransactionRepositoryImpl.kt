package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.domain.model.DailySummary
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.TransactionRepository

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> =
        transactionDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeByAccount(accountId: Long): Flow<List<Transaction>> =
        transactionDao.observeByAccount(accountId).map { rows -> rows.map { it.toDomain() } }

    override fun observeByPeriod(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactionDao.observeByPeriod(startMillis, endMillis).map { rows -> rows.map { it.toDomain() } }

    override fun observeDailySummaries(startMillis: Long, endMillis: Long): Flow<List<DailySummary>> =
        transactionDao.observeDailySummaries(startMillis, endMillis)
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: Long): Transaction? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun upsert(transaction: Transaction): Long {
        val entity = transaction.toEntity()
        return if (entity.id == 0L) {
            transactionDao.insert(entity)
        } else {
            transactionDao.update(entity)
            entity.id
        }
    }

    override suspend fun delete(id: Long) {
        transactionDao.deleteById(id)
    }

    override suspend fun sumByTypeInPeriod(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long,
        accountId: Long?,
    ): Long = transactionDao.sumByTypeInPeriod(type, startMillis, endMillis, accountId)
}
