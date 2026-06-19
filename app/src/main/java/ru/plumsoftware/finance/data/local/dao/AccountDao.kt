package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.AccountEntity

data class AccountWithBalanceRow(
    @Embedded val account: AccountEntity,
    val calculatedBalance: Long,
)

@Dao
interface AccountDao {

    @Query(
        """
        SELECT * FROM accounts
        WHERE isArchived = 0
        ORDER BY sortOrder ASC, id ASC
        """,
    )
    fun observeAllActive(): Flow<List<AccountEntity>>

    @Query(
        """
        SELECT * FROM accounts
        WHERE isArchived = 0
        ORDER BY sortOrder ASC, id ASC
        """,
    )
    suspend fun getAllActiveSync(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun observeById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity): Long

    @Update
    suspend fun update(account: AccountEntity)

    @Query(
        """
        UPDATE accounts SET isArchived = 1
        WHERE id = :id AND isDefault = 0
        """,
    )
    suspend fun archive(id: Long)

    @Query(
        """
        DELETE FROM accounts
        WHERE id = :id AND isDefault = 0
        """,
    )
    suspend fun delete(id: Long)

    @Query("UPDATE accounts SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)

    @Query(
        """
        SELECT
            a.id, a.name, a.type, a.currencyCode, a.colorHex,
            a.emoji, a.initialBalanceMinor, a.sortOrder,
            a.isDefault, a.isArchived, a.createdAtMillis,
            (
                a.initialBalanceMinor
                + COALESCE(SUM(
                    CASE WHEN t.type = 'INCOME'
                    THEN t.amountMinor ELSE 0 END), 0)
                - COALESCE(SUM(
                    CASE WHEN t.type = 'EXPENSE'
                    THEN t.amountMinor ELSE 0 END), 0)
                - COALESCE(SUM(
                    CASE WHEN t.type = 'SAVINGS'
                    THEN t.amountMinor ELSE 0 END), 0)
            ) AS calculatedBalance
        FROM accounts a
        LEFT JOIN transactions t ON t.accountId = a.id
        WHERE a.isArchived = 0
        GROUP BY a.id
        ORDER BY a.sortOrder ASC, a.id ASC
        """,
    )
    fun observeAllWithBalances(): Flow<List<AccountWithBalanceRow>>
}
