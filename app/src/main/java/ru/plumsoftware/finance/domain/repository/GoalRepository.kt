package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.GoalDeposit

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    fun observeFeaturedOnHome(): Flow<Goal?>
    fun observeGoal(goalId: Long): Flow<Goal?>
    fun observeDeposits(goalId: Long): Flow<List<GoalDeposit>>
    suspend fun getGoal(goalId: Long): Goal?
    suspend fun upsertGoal(goal: Goal): Long
    suspend fun addDeposit(goalId: Long, amountMinor: Long, note: String?): Goal
    suspend fun deleteGoal(goalId: Long)
}
