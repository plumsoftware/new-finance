package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.TransactionEntity
import ru.plumsoftware.finance.domain.model.TransactionType

data class DailySummaryRow(
    val dayStartMillis: Long,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

data class CategorySpendingRow(
    val categoryId: Long,
    val amountMinor: Long,
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE dateMillis >= :startMillis AND dateMillis < :endMillis
        ORDER BY dateMillis DESC, id DESC
        """,
    )
    fun observeByPeriod(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE transactions SET categoryId = :toId WHERE categoryId = :fromId")
    suspend fun reassignCategory(fromId: Long, toId: Long)

    @Query(
        """
        SELECT COALESCE(SUM(amountMinor), 0) FROM transactions
        WHERE type = :type
        AND dateMillis >= :startMillis AND dateMillis < :endMillis
        """,
    )
    suspend fun sumByTypeInPeriod(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long,
    ): Long

    @Query(
        """
        SELECT
            ((dateMillis / 86400000) * 86400000) AS dayStartMillis,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountMinor ELSE 0 END), 0) AS incomeMinor,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountMinor ELSE 0 END), 0) AS expenseMinor
        FROM transactions
        WHERE dateMillis >= :startMillis AND dateMillis < :endMillis
        AND type IN ('INCOME', 'EXPENSE')
        GROUP BY dayStartMillis
        ORDER BY dayStartMillis ASC
        """,
    )
    fun observeDailySummaries(startMillis: Long, endMillis: Long): Flow<List<DailySummaryRow>>

    @Query(
        """
        SELECT categoryId, COALESCE(SUM(amountMinor), 0) AS amountMinor
        FROM transactions
        WHERE type = 'EXPENSE'
        AND categoryId IS NOT NULL
        AND dateMillis >= :startMillis AND dateMillis < :endMillis
        GROUP BY categoryId
        ORDER BY amountMinor DESC
        LIMIT :limit
        """,
    )
    suspend fun getTopExpenseByCategory(
        startMillis: Long,
        endMillis: Long,
        limit: Int,
    ): List<CategorySpendingRow>

    @Query(
        """
        SELECT categoryId, COALESCE(SUM(amountMinor), 0) AS amountMinor
        FROM transactions
        WHERE type = 'EXPENSE'
        AND categoryId IS NOT NULL
        AND dateMillis >= :startMillis AND dateMillis < :endMillis
        GROUP BY categoryId
        """,
    )
    fun observeExpenseByCategoryForPeriod(
        startMillis: Long,
        endMillis: Long,
    ): Flow<List<CategorySpendingRow>>
}
