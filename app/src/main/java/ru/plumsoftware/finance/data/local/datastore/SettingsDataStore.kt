package ru.plumsoftware.finance.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.ThemeMode

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
)

class SettingsDataStore(
    private val context: Context,
) {
    private val dataStore = context.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            defaultCurrencyCode = prefs[Keys.DEFAULT_CURRENCY] ?: "RUB",
            selectedAccountId = prefs[Keys.SELECTED_ACCOUNT_ID] ?: 1L,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            themeMode = ThemeMode.entries.getOrElse(
                prefs[Keys.THEME_MODE]?.toIntOrNull() ?: 0,
            ) { ThemeMode.SYSTEM },
            permissionsPromptHidden = prefs[Keys.PERMISSIONS_PROMPT_HIDDEN] ?: false,
            initialBalancePromptCompleted = prefs[Keys.INITIAL_BALANCE_PROMPT_COMPLETED] ?: false,
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs ->
            val current = AppSettings(
                defaultCurrencyCode = prefs[Keys.DEFAULT_CURRENCY] ?: "RUB",
                selectedAccountId = prefs[Keys.SELECTED_ACCOUNT_ID] ?: 1L,
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
                themeMode = ThemeMode.entries.getOrElse(
                    prefs[Keys.THEME_MODE]?.toIntOrNull() ?: 0,
                ) { ThemeMode.SYSTEM },
                permissionsPromptHidden = prefs[Keys.PERMISSIONS_PROMPT_HIDDEN] ?: false,
                initialBalancePromptCompleted = prefs[Keys.INITIAL_BALANCE_PROMPT_COMPLETED] ?: false,
            )
            val updated = transform(current)
            prefs[Keys.DEFAULT_CURRENCY] = updated.defaultCurrencyCode
            prefs[Keys.SELECTED_ACCOUNT_ID] = updated.selectedAccountId
            prefs[Keys.ONBOARDING_COMPLETED] = updated.onboardingCompleted
            prefs[Keys.BIOMETRIC_ENABLED] = updated.biometricEnabled
            prefs[Keys.THEME_MODE] = updated.themeMode.ordinal.toString()
            prefs[Keys.PERMISSIONS_PROMPT_HIDDEN] = updated.permissionsPromptHidden
            prefs[Keys.INITIAL_BALANCE_PROMPT_COMPLETED] = updated.initialBalancePromptCompleted
        }
    }

    private object Keys {
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val SELECTED_ACCOUNT_ID = longPreferencesKey("selected_account_id")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PERMISSIONS_PROMPT_HIDDEN = booleanPreferencesKey("permissions_prompt_hidden")
        val INITIAL_BALANCE_PROMPT_COMPLETED = booleanPreferencesKey("initial_balance_prompt_completed")
    }
}
