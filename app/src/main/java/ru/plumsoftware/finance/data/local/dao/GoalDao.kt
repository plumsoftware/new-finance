package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.GoalDepositEntity
import ru.plumsoftware.finance.data.local.entity.GoalEntity

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, createdAtMillis DESC")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE showOnHome = 1 ORDER BY createdAtMillis DESC LIMIT 1")
    fun observeFeaturedOnHome(): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun observeGoal(goalId: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoal(goalId: Long): GoalEntity?

    @Query("SELECT * FROM goal_deposits WHERE goalId = :goalId ORDER BY createdAtMillis DESC")
    fun observeDeposits(goalId: Long): Flow<List<GoalDepositEntity>>

    @Query("SELECT * FROM goals ORDER BY createdAtMillis DESC")
    suspend fun getAllGoalsSync(): List<GoalEntity>

    @Query("SELECT * FROM goal_deposits ORDER BY createdAtMillis DESC")
    suspend fun getAllDepositsSync(): List<GoalDepositEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: GoalDepositEntity): Long

    @Query("SELECT * FROM goal_deposits WHERE id = :id")
    suspend fun getDepositById(id: Long): GoalDepositEntity?

    @Delete
    suspend fun deleteDeposit(deposit: GoalDepositEntity)

    @Query("DELETE FROM goal_deposits WHERE id = :depositId")
    suspend fun deleteDepositById(depositId: Long)

    @Query(
        """
        SELECT COALESCE(SUM(amountMinor), 0)
        FROM goal_deposits
        WHERE goalId = :goalId
        """,
    )
    suspend fun getTotalDepositsByGoalId(goalId: Long): Long

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: Long)

    @Query("DELETE FROM goal_deposits")
    suspend fun deleteAllDeposits()

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()

    @Query("UPDATE goals SET showOnHome = 0 WHERE id != :goalId")
    suspend fun clearShowOnHomeExcept(goalId: Long)

    @Transaction
    suspend fun upsertGoal(goal: GoalEntity): Long {
        val goalId = if (goal.id == 0L) {
            insertGoal(goal)
        } else {
            updateGoal(goal)
            goal.id
        }
        if (goal.showOnHome) {
            clearShowOnHomeExcept(goalId)
        }
        return goalId
    }
}
