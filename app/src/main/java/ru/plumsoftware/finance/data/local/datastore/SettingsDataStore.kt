package ru.plumsoftware.finance.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

    val settings: Flow<AppSettings> = dataStore.data.map { prefs -> prefs.toSettings() }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { prefs ->
            val updated = transform(prefs.toSettings())
            prefs.write(updated)
        }
    }

    private fun Preferences.toSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            defaultCurrencyCode = this[Keys.DEFAULT_CURRENCY] ?: defaults.defaultCurrencyCode,
            selectedAccountId = this[Keys.SELECTED_ACCOUNT_ID] ?: defaults.selectedAccountId,
            onboardingCompleted = this[Keys.ONBOARDING_COMPLETED] ?: false,
            biometricEnabled = this[Keys.BIOMETRIC_ENABLED] ?: false,
            themeMode = ThemeMode.entries.getOrElse(
                this[Keys.THEME_MODE]?.toIntOrNull() ?: 0,
            ) { ThemeMode.SYSTEM },
            permissionsPromptHidden = this[Keys.PERMISSIONS_PROMPT_HIDDEN] ?: false,
            initialBalancePromptCompleted = this[Keys.INITIAL_BALANCE_PROMPT_COMPLETED] ?: false,
            monthlyBudgetMinor = this[Keys.MONTHLY_BUDGET]?.takeIf { it > 0 },
            hideAmountsOnLaunch = this[Keys.HIDE_AMOUNTS] ?: defaults.hideAmountsOnLaunch,
            reminderEnabled = this[Keys.REMINDER_ENABLED] ?: defaults.reminderEnabled,
            reminderMinuteOfDay = this[Keys.REMINDER_MINUTE] ?: defaults.reminderMinuteOfDay,
            limitNotificationsEnabled = this[Keys.LIMIT_NOTIFICATIONS] ?: defaults.limitNotificationsEnabled,
            recurringNotificationsEnabled = this[Keys.RECURRING_NOTIFICATIONS] ?: defaults.recurringNotificationsEnabled,
            analyticsExcludedCategoryIds = this[Keys.ANALYTICS_EXCLUDED].orEmpty().mapNotNull { it.toLongOrNull() }.toSet(),
            kopiTipHiddenEpochDay = this[Keys.TIP_HIDDEN_DAY] ?: -1L,
            lastNeutralTipIndex = this[Keys.NEUTRAL_TIP_INDEX] ?: -1,
            lastNeutralTipEpochDay = this[Keys.NEUTRAL_TIP_DAY] ?: -1L,
            noSpendEpochDays = this[Keys.NO_SPEND_DAYS].orEmpty().mapNotNull { it.toLongOrNull() }.toSet(),
            reportsExported = this[Keys.REPORTS_EXPORTED] ?: 0,
            receiptsScanned = this[Keys.RECEIPTS_SCANNED] ?: 0,
            seenHints = this[Keys.SEEN_HINTS].orEmpty(),
        )
    }

    private fun MutablePreferences.write(s: AppSettings) {
        this[Keys.DEFAULT_CURRENCY] = s.defaultCurrencyCode
        this[Keys.SELECTED_ACCOUNT_ID] = s.selectedAccountId
        this[Keys.ONBOARDING_COMPLETED] = s.onboardingCompleted
        this[Keys.BIOMETRIC_ENABLED] = s.biometricEnabled
        this[Keys.THEME_MODE] = s.themeMode.ordinal.toString()
        this[Keys.PERMISSIONS_PROMPT_HIDDEN] = s.permissionsPromptHidden
        this[Keys.INITIAL_BALANCE_PROMPT_COMPLETED] = s.initialBalancePromptCompleted
        this[Keys.MONTHLY_BUDGET] = s.monthlyBudgetMinor ?: 0L
        this[Keys.HIDE_AMOUNTS] = s.hideAmountsOnLaunch
        this[Keys.REMINDER_ENABLED] = s.reminderEnabled
        this[Keys.REMINDER_MINUTE] = s.reminderMinuteOfDay
        this[Keys.LIMIT_NOTIFICATIONS] = s.limitNotificationsEnabled
        this[Keys.RECURRING_NOTIFICATIONS] = s.recurringNotificationsEnabled
        this[Keys.ANALYTICS_EXCLUDED] = s.analyticsExcludedCategoryIds.map { it.toString() }.toSet()
        this[Keys.TIP_HIDDEN_DAY] = s.kopiTipHiddenEpochDay
        this[Keys.NEUTRAL_TIP_INDEX] = s.lastNeutralTipIndex
        this[Keys.NEUTRAL_TIP_DAY] = s.lastNeutralTipEpochDay
        // Храним только последние 120 дней «без трат».
        this[Keys.NO_SPEND_DAYS] = s.noSpendEpochDays.sortedDescending().take(120).map { it.toString() }.toSet()
        this[Keys.REPORTS_EXPORTED] = s.reportsExported
        this[Keys.RECEIPTS_SCANNED] = s.receiptsScanned
        this[Keys.SEEN_HINTS] = s.seenHints
    }

    private object Keys {
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val SELECTED_ACCOUNT_ID = longPreferencesKey("selected_account_id")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PERMISSIONS_PROMPT_HIDDEN = booleanPreferencesKey("permissions_prompt_hidden")
        val INITIAL_BALANCE_PROMPT_COMPLETED = booleanPreferencesKey("initial_balance_prompt_completed")
        val MONTHLY_BUDGET = longPreferencesKey("monthly_budget_minor")
        val HIDE_AMOUNTS = booleanPreferencesKey("hide_amounts_on_launch")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute_of_day")
        val LIMIT_NOTIFICATIONS = booleanPreferencesKey("limit_notifications_enabled")
        val RECURRING_NOTIFICATIONS = booleanPreferencesKey("recurring_notifications_enabled")
        val ANALYTICS_EXCLUDED = stringSetPreferencesKey("analytics_excluded_categories")
        val TIP_HIDDEN_DAY = longPreferencesKey("kopi_tip_hidden_epoch_day")
        val NEUTRAL_TIP_INDEX = intPreferencesKey("kopi_neutral_tip_index")
        val NEUTRAL_TIP_DAY = longPreferencesKey("kopi_neutral_tip_epoch_day")
        val NO_SPEND_DAYS = stringSetPreferencesKey("no_spend_epoch_days")
        val REPORTS_EXPORTED = intPreferencesKey("reports_exported")
        val RECEIPTS_SCANNED = intPreferencesKey("receipts_scanned")
        val SEEN_HINTS = stringSetPreferencesKey("seen_hints")
    }
}
