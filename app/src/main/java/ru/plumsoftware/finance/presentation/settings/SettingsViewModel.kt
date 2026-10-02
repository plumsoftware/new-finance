package ru.plumsoftware.finance.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.notifications.DailyNotificationsWorker
import ru.plumsoftware.finance.domain.achievements.AchievementId
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.PermissionsRepository
import ru.plumsoftware.finance.domain.repository.RecurringRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.BiometricUtils
import ru.plumsoftware.finance.presentation.common.DateFmt
import java.time.LocalDate

data class SettingsCounts(
    val accounts: Int = 0,
    val categories: Int = 0,
    val limitsSet: Int = 0,
    val expenseCategories: Int = 0,
    val goals: Int = 0,
    val achievements: Int = 0,
    val recurring: Int = 0,
    val firstOperation: LocalDate? = null,
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val counts: SettingsCounts = SettingsCounts(),
    val biometricAvailable: Boolean = false,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val permissionsRepository: PermissionsRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    goalRepository: GoalRepository,
    recurringRepository: RecurringRepository,
    transactionRepository: TransactionRepository,
    achievementUnlockDao: AchievementUnlockDao,
    private val context: Context,
) : ViewModel() {

    private val biometricAvailable = BiometricUtils.isAvailable(context)

    val deniedPermissionsCount: StateFlow<Int> = permissionsRepository.permissionStates
        .map { list -> list.count { !it.isGranted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val counts = combine(
        combine(
            accountRepository.observeAllActive(),
            categoryRepository.observeByType(CategoryType.EXPENSE, false),
            categoryRepository.observeByType(CategoryType.INCOME, false),
        ) { acc, exp, inc -> Triple(acc.size, exp, inc.size) },
        goalRepository.observeGoals(),
        recurringRepository.observeAll(),
        achievementUnlockDao.observeAll(),
        transactionRepository.observeAll(),
    ) { (acc, exp, inc), goals, rec, unlocks, tx ->
        SettingsCounts(
            accounts = acc,
            categories = exp.size + inc,
            limitsSet = exp.count { (it.monthlyLimitMinor ?: 0) > 0 },
            expenseCategories = exp.size,
            goals = goals.size,
            achievements = unlocks.count { it.key in AchievementId.keys },
            recurring = rec.size,
            firstOperation = tx.minOfOrNull { it.dateMillis }?.let(DateFmt::toLocalDate),
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(settingsRepository.settings, counts) { s, c ->
        SettingsUiState(s, c, biometricAvailable)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    fun setBiometric(enabled: Boolean) = update { it.copy(biometricEnabled = enabled) }
    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setCurrency(code: String) = update { it.copy(defaultCurrencyCode = code) }
    fun setHideAmounts(v: Boolean) = update { it.copy(hideAmountsOnLaunch = v) }
    fun setLimitNotifications(v: Boolean) = update { it.copy(limitNotificationsEnabled = v) }
    fun setRecurringNotifications(v: Boolean) = update { it.copy(recurringNotificationsEnabled = v) }
    fun setReminder(v: Boolean) = update { it.copy(reminderEnabled = v) }

    fun setReminderTime(minuteOfDay: Int) {
        update { it.copy(reminderMinuteOfDay = minuteOfDay, reminderEnabled = true) }
        DailyNotificationsWorker.schedule(context, minuteOfDay, replace = true)
    }

    fun refreshPermissions(activity: android.app.Activity) {
        permissionsRepository.refresh(activity)
    }
}
