package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.RecurringTransactionEntity

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY isActive DESC, nextDateMillis ASC")
    fun observeAll(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getById(id: Long): RecurringTransactionEntity?

    @Query(
        """
        SELECT * FROM recurring_transactions
        WHERE isActive = 1 AND nextDateMillis <= :todayEndMillis
        """,
    )
    suspend fun getDue(todayEndMillis: Long): List<RecurringTransactionEntity>

    @Insert
    suspend fun insert(entity: RecurringTransactionEntity): Long

    @Update
    suspend fun update(entity: RecurringTransactionEntity)

    @Delete
    suspend fun delete(entity: RecurringTransactionEntity)

    @Query("UPDATE recurring_transactions SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}
