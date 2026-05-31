package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.RecurringTransaction

interface RecurringRepository {
    fun observeAll(): Flow<List<RecurringTransaction>>
    suspend fun add(transaction: RecurringTransaction): Long
    suspend fun toggleActive(id: Long, active: Boolean)
    suspend fun delete(id: Long)
    suspend fun processDueTransactions()
}
