package ru.plumsoftware.finance.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.domain.repository.PermissionsRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.BiometricUtils

data class SettingsUiState(
    val currencyCode: String = "RUB",
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val permissionsRepository: PermissionsRepository,
    context: Context,
) : ViewModel() {

    private val biometricAvailable = BiometricUtils.isAvailable(context)
    private val themeModeOverride = MutableStateFlow<ThemeMode?>(null)

    val deniedPermissionsCount: StateFlow<Int> = permissionsRepository.permissionStates
        .map { list -> list.count { !it.isGranted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        themeModeOverride,
    ) { settings, themeOverride ->
        SettingsUiState(
            currencyCode = settings.defaultCurrencyCode,
            biometricEnabled = settings.biometricEnabled,
            biometricAvailable = biometricAvailable,
            themeMode = themeOverride ?: settings.themeMode,
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

    fun refreshPermissions(activity: android.app.Activity) {
        permissionsRepository.refresh(activity)
    }
}
