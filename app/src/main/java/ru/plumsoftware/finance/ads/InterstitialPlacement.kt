package ru.plumsoftware.finance.ads

import ru.plumsoftware.finance.AppConfig

enum class InterstitialPlacement {
    TRANSACTION,
    GOAL,
    SMART_SAVINGS,
    ;

    fun adUnitId(): String = when (this) {
        TRANSACTION -> AppConfig.interstitialAfterCreateTransaction
        GOAL -> AppConfig.interstitialAfterCreateGoal
        SMART_SAVINGS -> AppConfig.interstitialAfterCreateSmartSavings
    }
}
