package ru.plumsoftware.finance.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class MascotEmotion {
    FULL_BODY,
    HAPPY,
    SLEEPING,
    EXCITED,
    SAD,
    PARTY,
    THINKING,
    STREAK,
    TROPHY,
    EMPTY_NOTIFICATIONS,
    EMPTY_SMART_SAVINGS,
    ONBOARDING_SMART,
}

fun MascotEmotion.toDrawableRes(): Int = when (this) {
    MascotEmotion.FULL_BODY -> MascotAssets.fullBody
    MascotEmotion.HAPPY -> MascotAssets.happy
    MascotEmotion.SLEEPING -> MascotAssets.sleeping
    MascotEmotion.EXCITED -> MascotAssets.excited
    MascotEmotion.SAD -> MascotAssets.sad
    MascotEmotion.PARTY -> MascotAssets.party
    MascotEmotion.THINKING -> MascotAssets.thinking
    MascotEmotion.STREAK -> MascotAssets.streak
    MascotEmotion.TROPHY -> MascotAssets.trophy
    MascotEmotion.EMPTY_NOTIFICATIONS -> MascotAssets.emptyNotifications
    MascotEmotion.EMPTY_SMART_SAVINGS -> MascotAssets.emptySmartSavings
    MascotEmotion.ONBOARDING_SMART -> MascotAssets.onboardingSmart
}

object MascotSize {
    val Small: Dp = 40.dp
    val Medium: Dp = 72.dp
    val Large: Dp = 100.dp
    val Hero: Dp = 130.dp
}
