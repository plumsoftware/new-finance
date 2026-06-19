package ru.plumsoftware.finance.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.GoalDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.entity.TransactionEntity
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.GoalDeposit
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.GoalRepository

class GoalRepositoryImpl(
    private val database: FinanceDatabase,
    private val goalDao: GoalDao,
    private val transactionDao: TransactionDao,
) : GoalRepository {
    override fun observeGoals(): Flow<List<Goal>> =
        goalDao.observeGoals().map { rows -> rows.map { it.toDomain() } }

    override fun observeFeaturedOnHome(): Flow<Goal?> =
        goalDao.observeFeaturedOnHome().map { row -> row?.toDomain() }

    override fun observeGoal(goalId: Long): Flow<Goal?> =
        goalDao.observeGoal(goalId).map { row -> row?.toDomain() }

    override fun observeDeposits(goalId: Long): Flow<List<GoalDeposit>> =
        goalDao.observeDeposits(goalId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getGoal(goalId: Long): Goal? =
        goalDao.getGoal(goalId)?.toDomain()

    override suspend fun upsertGoal(goal: Goal): Long {
        val correctedSaved = goal.savedAmountMinor.coerceAtMost(goal.targetAmountMinor).coerceAtLeast(0L)
        val corrected = goal.copy(
            savedAmountMinor = correctedSaved,
            isCompleted = correctedSaved >= goal.targetAmountMinor && goal.targetAmountMinor > 0L,
        )
        return goalDao.upsertGoal(corrected.toEntity())
    }

    override suspend fun addDeposit(
        goalId: Long,
        amountMinor: Long,
        note: String?,
        currencyCode: String,
        accountId: Long?,
        transactionNote: String,
    ): Goal =
        database.withTransaction {
            val current = goalDao.getGoal(goalId)?.toDomain()
                ?: error("Goal $goalId not found")
            val now = System.currentTimeMillis()
            val resolvedAccountId = accountId ?: current.accountId ?: 1L
            val updatedSaved = (current.savedAmountMinor + amountMinor).coerceAtMost(current.targetAmountMinor)
            val updated = current.copy(
                savedAmountMinor = updatedSaved,
                isCompleted = updatedSaved >= current.targetAmountMinor && current.targetAmountMinor > 0L,
            )
            val transactionId = transactionDao.insert(
                TransactionEntity(
                    type = TransactionType.SAVINGS,
                    amountMinor = amountMinor,
                    categoryId = null,
                    smartAssetId = null,
                    goalId = goalId,
                    note = transactionNote,
                    dateMillis = now,
                    createdAtMillis = now,
                    accountId = resolvedAccountId,
                    currencyCode = currencyCode,
                    originalAmountMinor = amountMinor,
                    originalCurrencyCode = currencyCode,
                    exchangeRate = 1.0,
                ),
            )
            goalDao.updateGoal(updated.toEntity())
            goalDao.insertDeposit(
                GoalDeposit(
                    goalId = goalId,
                    amountMinor = amountMinor,
                    note = note?.takeIf { it.isNotBlank() },
                    createdAtMillis = now,
                    currencyCode = currencyCode,
                    accountId = resolvedAccountId,
                    transactionId = transactionId,
                ).toEntity(),
            )
            updated
        }

    override suspend fun deleteDeposit(deposit: GoalDeposit) {
        database.withTransaction {
            val goal = goalDao.getGoal(deposit.goalId)?.toDomain() ?: return@withTransaction
            deposit.transactionId?.let { transactionDao.deleteById(it) }
            if (deposit.id > 0L) {
                goalDao.deleteDepositById(deposit.id)
            } else {
                goalDao.deleteDeposit(deposit.toEntity())
            }
            val totalFromDeposits = goalDao.getTotalDepositsByGoalId(deposit.goalId)
            val afterSubtract = (goal.savedAmountMinor - deposit.amountMinor).coerceAtLeast(0L)
            val newSaved = maxOf(totalFromDeposits, afterSubtract)
                .coerceAtMost(goal.targetAmountMinor)
            val updated = goal.copy(
                savedAmountMinor = newSaved,
                isCompleted = newSaved >= goal.targetAmountMinor && goal.targetAmountMinor > 0L,
            )
            goalDao.updateGoal(updated.toEntity())
        }
    }

    override suspend fun deleteDepositByTransactionId(transactionId: Long) {
        val deposit = goalDao.getDepositByTransactionId(transactionId)?.toDomain() ?: run {
            transactionDao.deleteById(transactionId)
            return
        }
        deleteDeposit(deposit)
    }

    override suspend fun deleteGoal(goalId: Long) {
        database.withTransaction {
            goalDao.getDepositsSync(goalId).forEach { deposit ->
                deposit.transactionId?.let { transactionDao.deleteById(it) }
            }
            goalDao.deleteGoal(goalId)
        }
    }
}
