package ru.plumsoftware.finance.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.BiometricUtils

data class SettingsUiState(
    val currencyCode: String = "RUB",
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accounts: List<AccountWithBalance> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    context: Context,
) : ViewModel() {

    private val biometricAvailable = BiometricUtils.isAvailable(context)
    private val themeModeOverride = MutableStateFlow<ThemeMode?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        themeModeOverride,
        accountRepository.observeVisibleWithBalances(),
        categoryRepository.observeByType(CategoryType.EXPENSE, true),
        categoryRepository.observeByType(CategoryType.INCOME, true),
    ) { settings, themeOverride, accounts, expense, income ->
        SettingsUiState(
            currencyCode = settings.defaultCurrencyCode,
            biometricEnabled = settings.biometricEnabled,
            biometricAvailable = biometricAvailable,
            themeMode = themeOverride ?: settings.themeMode,
            accounts = accounts,
            expenseCategories = expense,
            incomeCategories = income,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(biometricEnabled = enabled) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        themeModeOverride.update { mode }
        viewModelScope.launch {
            settingsRepository.update { it.copy(themeMode = mode) }
            themeModeOverride.update { null }
        }
    }

    fun toggleCategoryHidden(id: Long, hidden: Boolean) {
        viewModelScope.launch {
            categoryRepository.setHidden(id, hidden)
        }
    }
}
