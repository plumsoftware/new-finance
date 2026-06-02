package ru.plumsoftware.finance.presentation.achievements

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.local.entity.AchievementUnlockEntity
import ru.plumsoftware.finance.data.repository.StreakRepository
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository

class AchievementsViewModel(
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    private val smartAssetRepository: SmartAssetRepository,
    private val streakRepository: StreakRepository,
    private val unlockDao: AchievementUnlockDao,
    private val context: Context,
) : ViewModel() {

    private val _pendingAchievement = MutableStateFlow<Achievement?>(null)
    val pendingAchievement: StateFlow<Achievement?> = _pendingAchievement.asStateFlow()

    val uiState: StateFlow<AchievementsUiState> = combine(
        transactionRepository.observeAll(),
        goalRepository.observeGoals(),
        smartAssetRepository.observeActive(),
        streakRepository.observe(),
        unlockDao.observeAll(),
    ) { tx, goals, assets, streak, unlocks ->
        val unlockedMap = unlocks.associateBy { it.key }
        val specs = buildSpecs(
            transactions = tx,
            goals = goals,
            assets = assets,
            streakDays = streak.currentStreak,
        )
        val unlocked = specs
            .filter { unlockedMap.containsKey(it.key) }
            .map { it.toAchievement(context, unlockedMap[it.key]?.unlockedAtMillis) }
            .sortedByDescending { it.unlockedAtMillis ?: 0L }
        val locked = specs
            .filterNot { unlockedMap.containsKey(it.key) }
            .map { it.toAchievement(context, unlockedAtMillis = null) }
        AchievementsUiState(
            streak = streak.copy(totalUnlocked = unlocked.size),
            unlocked = unlocked,
            locked = locked,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AchievementsUiState())

    init {
        viewModelScope.launch {
            combine(
                transactionRepository.observeAll(),
                goalRepository.observeGoals(),
                smartAssetRepository.observeActive(),
                streakRepository.observe(),
                unlockDao.observeAll(),
            ) { tx, goals, assets, streak, unlocks ->
                AchievementSnapshot(
                    specs = buildSpecs(tx, goals, assets, streak.currentStreak),
                    unlocks = unlocks,
                    currentStreak = streak.currentStreak,
                )
            }.collect { snapshot ->
                var firstNew: Achievement? = null
                var added = 0
                val unlockedKeys = snapshot.unlocks.map { it.key }.toSet()
                snapshot.specs.forEach { spec ->
                    if (spec.isUnlocked && spec.key !in unlockedKeys) {
                        val now = System.currentTimeMillis()
                        val inserted = unlockDao.insert(AchievementUnlockEntity(spec.key, now))
                        if (inserted != -1L) {
                            added++
                            if (firstNew == null) {
                                firstNew = spec.toAchievement(context, now)
                            }
                        }
                    }
                }
                if (firstNew != null) {
                    _pendingAchievement.value = firstNew
                }
                val totalUnlocked = snapshot.unlocks.size + added
                streakRepository.setTotalUnlocked(totalUnlocked)
            }
        }
    }

    fun clearPending() {
        _pendingAchievement.value = null
    }

    private fun buildSpecs(
        transactions: List<Transaction>,
        goals: List<Goal>,
        assets: List<SmartAsset>,
        streakDays: Int,
    ): List<AchievementSpec> {
        val txCount = transactions.size
        val completedGoals = goals.count { it.isCompleted }
        val hasAnyGoal = goals.isNotEmpty()
        val hasAnyAsset = assets.isNotEmpty()
        val hasPaidOffAsset = assets.any { it.totalSavedMinor >= it.purchaseCostMinor }
        val positiveMonth = isCurrentMonthPositive(transactions)

        return listOf(
            AchievementSpec(AchievementKeys.FIRST_TX, "👣", R.string.ach_first_tx_title, R.string.ach_first_tx_desc, txCount, 1),
            AchievementSpec(AchievementKeys.STREAK_7, "🚀", R.string.ach_streak_7_title, R.string.ach_streak_7_desc, streakDays, 7, progressDays = true),
            AchievementSpec(AchievementKeys.STREAK_30, "🏆", R.string.ach_streak_30_title, R.string.ach_streak_30_desc, streakDays, 30, progressDays = true),
            AchievementSpec(AchievementKeys.STREAK_100, "🌟", R.string.ach_streak_100_title, R.string.ach_streak_100_desc, streakDays, 100, progressDays = true),
            AchievementSpec(AchievementKeys.TX_10, "🧾", R.string.ach_tx_10_title, R.string.ach_tx_10_desc, txCount, 10),
            AchievementSpec(AchievementKeys.TX_50, "📒", R.string.ach_tx_50_title, R.string.ach_tx_50_desc, txCount, 50),
            AchievementSpec(AchievementKeys.TX_100, "💯", R.string.ach_tx_100_title, R.string.ach_tx_100_desc, txCount, 100),
            AchievementSpec(AchievementKeys.FIRST_GOAL, "🎯", R.string.ach_first_goal_title, R.string.ach_first_goal_desc, if (hasAnyGoal) 1 else 0, 1),
            AchievementSpec(AchievementKeys.GOAL_DONE, "🥳", R.string.ach_goal_done_title, R.string.ach_goal_done_desc, completedGoals, 1),
            AchievementSpec(AchievementKeys.FIRST_SAVING, "🦉", R.string.ach_first_saving_title, R.string.ach_first_saving_desc, if (hasAnyAsset) 1 else 0, 1),
            AchievementSpec(AchievementKeys.PAID_OFF, "💸", R.string.ach_paid_off_title, R.string.ach_paid_off_desc, if (hasPaidOffAsset) 1 else 0, 1),
            AchievementSpec(AchievementKeys.POSITIVE_MONTH, "🟢", R.string.ach_positive_month_title, R.string.ach_positive_month_desc, if (positiveMonth) 1 else 0, 1),
        )
    }

    private fun isCurrentMonthPositive(transactions: List<Transaction>): Boolean {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        val inMonth = transactions.filter { it.dateMillis in start.timeInMillis until end.timeInMillis }
        if (inMonth.isEmpty()) return false
        val income = inMonth.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
        val expense = inMonth.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
        return income > expense
    }
}

private data class AchievementSpec(
    val key: String,
    val emoji: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val progressValue: Int,
    val targetValue: Int,
    val progressDays: Boolean = false,
) {
    val isUnlocked: Boolean get() = progressValue >= targetValue
}

private data class AchievementSnapshot(
    val specs: List<AchievementSpec>,
    val unlocks: List<AchievementUnlockEntity>,
    val currentStreak: Int,
)

private fun AchievementSpec.toAchievement(context: Context, unlockedAtMillis: Long?): Achievement {
    val title = context.getString(titleRes)
    val description = context.getString(descriptionRes)
    val progress = (progressValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
    return Achievement(
        key = key,
        title = title,
        description = description,
        emoji = emoji,
        unlockedAtMillis = unlockedAtMillis,
        progress = if (unlockedAtMillis == null) progress else null,
        progressLabel = if (unlockedAtMillis == null) {
            if (progressDays) {
                context.getString(R.string.ach_progress_days, progressValue.coerceAtMost(targetValue), targetValue)
            } else {
                context.getString(R.string.ach_progress_count, progressValue.coerceAtMost(targetValue), targetValue)
            }
        } else {
            null
        },
    )
}
