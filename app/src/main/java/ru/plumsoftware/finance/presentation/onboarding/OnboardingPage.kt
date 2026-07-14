package ru.plumsoftware.finance.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class OnboardingIllustrationType {
    CONTROL_CHART,
    INCOME_EXPENSE,
    SMART_SAVINGS,
    GOALS,
    ACHIEVEMENTS,
    TOOLS,
    WELCOME,
}

data class OnboardingPage(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    @DrawableRes val mascotRes: Int,
    val illustrationType: OnboardingIllustrationType,
)
