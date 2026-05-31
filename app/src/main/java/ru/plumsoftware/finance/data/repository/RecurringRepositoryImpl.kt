package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.RecurringTransactionDao
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.RecurringFrequency
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.RecurringRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import java.util.Calendar

class RecurringRepositoryImpl(
    private val recurringDao: RecurringTransactionDao,
    private val transactionRepository: TransactionRepository,
) : RecurringRepository {

    override fun observeAll(): Flow<List<RecurringTransaction>> =
        recurringDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(transaction: RecurringTransaction): Long =
        recurringDao.insert(transaction.toEntity())

    override suspend fun toggleActive(id: Long, active: Boolean) {
        recurringDao.setActive(id, active)
    }

    override suspend fun delete(id: Long) {
        val entity = recurringDao.getById(id) ?: return
        recurringDao.delete(entity)
    }

    override suspend fun processDueTransactions() {
        val todayEnd = endOfDayMillis(System.currentTimeMillis())
        val due = recurringDao.getDue(todayEnd)
        due.forEach { entity ->
            val item = entity.toDomain()
            transactionRepository.upsert(
                Transaction(
                    type = if (item.isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                    amountMinor = item.amountMinor,
                    categoryId = item.categoryId,
                    smartAssetId = null,
                    note = item.title,
                    dateMillis = System.currentTimeMillis(),
                    createdAtMillis = System.currentTimeMillis(),
                ),
            )
            val nextMillis = advanceNextDate(item.nextDateMillis, item.frequency, item.dayOfMonth)
            recurringDao.update(
                entity.copy(nextDateMillis = nextMillis),
            )
        }
    }

    private fun advanceNextDate(
        currentMillis: Long,
        frequency: RecurringFrequency,
        dayOfMonth: Int?,
    ): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentMillis }
        when (frequency) {
            RecurringFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.MONTHLY -> {
                cal.add(Calendar.MONTH, 1)
                dayOfMonth?.let { cal.set(Calendar.DAY_OF_MONTH, it.coerceIn(1, 28)) }
            }
            RecurringFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return startOfDayMillis(cal.timeInMillis)
    }
}
