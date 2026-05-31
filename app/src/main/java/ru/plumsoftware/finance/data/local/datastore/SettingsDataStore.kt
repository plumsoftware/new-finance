package ru.plumsoftware.finance.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
            themeMode = ThemeMode.entries.getOrElse(
                prefs[Keys.THEME_MODE]?.toIntOrNull() ?: 0,
            ) { ThemeMode.SYSTEM },
            permissionsPromptHidden = prefs[Keys.PERMISSIONS_PROMPT_HIDDEN] ?: false,
        )
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs ->
            val current = AppSettings(
                defaultCurrencyCode = prefs[Keys.DEFAULT_CURRENCY] ?: "RUB",
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
                themeMode = ThemeMode.entries.getOrElse(
                    prefs[Keys.THEME_MODE]?.toIntOrNull() ?: 0,
                ) { ThemeMode.SYSTEM },
                permissionsPromptHidden = prefs[Keys.PERMISSIONS_PROMPT_HIDDEN] ?: false,
            )
            val updated = transform(current)
            prefs[Keys.DEFAULT_CURRENCY] = updated.defaultCurrencyCode
            prefs[Keys.ONBOARDING_COMPLETED] = updated.onboardingCompleted
            prefs[Keys.BIOMETRIC_ENABLED] = updated.biometricEnabled
            prefs[Keys.THEME_MODE] = updated.themeMode.ordinal.toString()
            prefs[Keys.PERMISSIONS_PROMPT_HIDDEN] = updated.permissionsPromptHidden
        }
    }

    private object Keys {
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PERMISSIONS_PROMPT_HIDDEN = booleanPreferencesKey("permissions_prompt_hidden")
    }
}
