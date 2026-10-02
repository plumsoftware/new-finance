package ru.plumsoftware.finance.presentation.achievements

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.local.entity.AchievementUnlockEntity
import ru.plumsoftware.finance.data.repository.StreakRepository
import ru.plumsoftware.finance.domain.achievements.AchievementId
import ru.plumsoftware.finance.domain.achievements.AchievementInputs
import ru.plumsoftware.finance.domain.achievements.AchievementProgress
import ru.plumsoftware.finance.domain.achievements.AchievementsEngine
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.StreakData
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import java.time.LocalDate

class AchievementsViewModel(
    transactionRepository: TransactionRepository,
    goalRepository: GoalRepository,
    smartAssetRepository: SmartAssetRepository,
    private val streakRepository: StreakRepository,
    private val unlockDao: AchievementUnlockDao,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
    private val context: Context,
) : ViewModel() {

    private val _pendingAchievement = MutableStateFlow<Achievement?>(null)
    val pendingAchievement: StateFlow<Achievement?> = _pendingAchievement.asStateFlow()

    private val progressFlow: Flow<Pair<List<AchievementProgress>, StreakData>> = combine(
        transactionRepository.observeAll(),
        goalRepository.observeGoals(),
        smartAssetRepository.observeTotalSavedAllTime(),
        streakRepository.observe(),
        combine(
            categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
            settingsRepository.settings,
        ) { cats, settings -> cats to settings },
    ) { tx, goals, smartSaved, streak, (cats, settings) ->
        val limits = cats.mapNotNull { c -> c.monthlyLimitMinor?.takeIf { it > 0 }?.let { c.id to it } }.toMap()
        val expenses = tx.filter { it.type == TransactionType.EXPENSE }
            .map { AchievementsEngine.Expense(DateFmt.toLocalDate(it.dateMillis), it.categoryId, it.amountMinor) }
        val inputs = AchievementInputs(
            transactionsCount = tx.count { it.type != TransactionType.SAVINGS },
            streakDays = maxOf(streak.currentStreak, 0),
            completedGoals = goals.count { it.isCompleted || (it.targetAmountMinor > 0 && it.savedAmountMinor >= it.targetAmountMinor) },
            smartSavedRub = smartSaved / 100,
            daysWithinLimits = AchievementsEngine.daysWithinLimits(expenses, limits, LocalDate.now()),
            goalsSavedRub = goals.sumOf { it.savedAmountMinor } / 100,
            reportsExported = settings.reportsExported,
            receiptsScanned = settings.receiptsScanned,
        )
        AchievementsEngine.progress(inputs) to streak
    }

    val uiState: StateFlow<AchievementsUiState> = combine(progressFlow, unlockDao.observeAll()) { (progress, streak), unlocks ->
        val unlockedMap = unlocks.filter { it.key in AchievementId.keys }.associateBy { it.key }
        val items = progress.map { it.toAchievement(unlockedMap[it.id.key]?.unlockedAtMillis) }
        AchievementsUiState(
            streak = streak.copy(totalUnlocked = items.count { it.isUnlocked }),
            achievements = items,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AchievementsUiState())

    init {
        viewModelScope.launch {
            combine(progressFlow, unlockDao.observeAll()) { p, u -> p.first to u }.collect { (progress, unlocks) ->
                val unlockedKeys = unlocks.map { it.key }.toSet()
                var firstNew: Achievement? = null
                progress.filter { it.isReached && it.id.key !in unlockedKeys }.forEach { p ->
                    val now = System.currentTimeMillis()
                    if (unlockDao.insert(AchievementUnlockEntity(p.id.key, now)) != -1L && firstNew == null) {
                        firstNew = p.toAchievement(now)
                    }
                }
                firstNew?.let { _pendingAchievement.value = it }
                val total = (unlockedKeys.filter { it in AchievementId.keys }.size +
                    progress.count { it.isReached && it.id.key !in unlockedKeys })
                streakRepository.setTotalUnlocked(total)
            }
        }
    }

    fun clearPending() {
        _pendingAchievement.value = null
    }

    private fun AchievementProgress.toAchievement(unlockedAt: Long?) = Achievement(
        id = id,
        title = context.getString(AchievementUi.titleRes(id)),
        description = context.getString(AchievementUi.descRes(id)),
        emoji = AchievementUi.emoji(id),
        value = value,
        unlockedAtMillis = unlockedAt,
    )
}
