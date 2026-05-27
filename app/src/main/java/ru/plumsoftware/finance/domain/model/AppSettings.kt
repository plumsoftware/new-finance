package ru.plumsoftware.finance.domain.model

data class AppSettings(
    val defaultCurrencyCode: String = "RUB",
    val onboardingCompleted: Boolean = false,
    val biometricEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
