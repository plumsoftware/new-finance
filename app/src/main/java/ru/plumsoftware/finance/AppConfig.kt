package ru.plumsoftware.finance

/**
1 - RuStore
2 - Google Play
3 - Huawei App Gallery
**/

object AppConfig {
    val interstitialAfterCreateTransaction: String = when (BuildConfig.PLATFORM) {
        1 -> ""
        2 -> ""
        3 -> ""
        else -> ""
    }

    val bannerOnCreateTransaction: String = when (BuildConfig.PLATFORM) {
        1 -> ""
        2 -> ""
        3 -> ""
        else -> ""
    }

    val bannerOnCreateEccoActive: String = when (BuildConfig.PLATFORM) {
        1 -> ""
        2 -> ""
        3 -> ""
        else -> ""
    }
}
