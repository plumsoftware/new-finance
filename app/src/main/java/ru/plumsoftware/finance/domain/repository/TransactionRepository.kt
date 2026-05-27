package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.DailySummary
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeByPeriod(startMillis: Long, endMillis: Long): Flow<List<Transaction>>
    fun observeDailySummaries(startMillis: Long, endMillis: Long): Flow<List<DailySummary>>
    suspend fun getById(id: Long): Transaction?
    suspend fun upsert(transaction: Transaction): Long
    suspend fun delete(id: Long)
    suspend fun sumByTypeInPeriod(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long,
    ): Long
}
