package ru.plumsoftware.finance.domain.model

data class AppSettings(
    val defaultCurrencyCode: String = "RUB",
    val selectedAccountId: Long = 1L,
    val onboardingCompleted: Boolean = false,
    val biometricEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val permissionsPromptHidden: Boolean = false,
    val initialBalancePromptCompleted: Boolean = false,
)
