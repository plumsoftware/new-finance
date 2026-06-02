package ru.plumsoftware.finance

/**
 * Конфигурация по магазину и рекламные блоки РСЯ.
 *
 * - [BuildConfig.PLATFORM]: 1 — RuStore, 2 — Google Play, 3 — Huawei
 * - RuStore: `R-M-…` в release, `demo-*-yandex` в debug
 * - Другие платформы: пустые ad unit
 *
 * См. `docs/YANDEX_ADS.md` в корне репозитория.
 */
object AppConfig {

    /** Минимальный интервал между показами любых межстраничных объявлений. */
    const val INTERSTITIAL_COOLDOWN_MS: Long = 3 * 60 * 1000L

    private object Demo {
        const val INTERSTITIAL = "demo-interstitial-yandex"
        const val BANNER = "demo-banner-yandex"
    }

    private object RuStore {
        const val BANNER_CATEGORIES = "R-M-19374501-1"
        const val BANNER_GOALS = "R-M-19374501-2"
        const val BANNER_LIMITS = "R-M-19374501-3"
        const val BANNER_RECURRING = "R-M-19374501-4"
        const val INTERSTITIAL_TRANSACTION = "R-M-19374501-5"
        const val INTERSTITIAL_GOAL = "R-M-19374501-6"
        const val INTERSTITIAL_SMART_SAVINGS = "R-M-19374501-7"
    }

    val interstitialAfterCreateTransaction: String =
        adUnit(RuStore.INTERSTITIAL_TRANSACTION, Demo.INTERSTITIAL)

    val interstitialAfterCreateGoal: String =
        adUnit(RuStore.INTERSTITIAL_GOAL, Demo.INTERSTITIAL)

    val interstitialAfterCreateSmartSavings: String =
        adUnit(RuStore.INTERSTITIAL_SMART_SAVINGS, Demo.INTERSTITIAL)

    val bannerCategories: String = adUnit(RuStore.BANNER_CATEGORIES, Demo.BANNER)

    val bannerGoals: String = adUnit(RuStore.BANNER_GOALS, Demo.BANNER)

    val bannerLimits: String = adUnit(RuStore.BANNER_LIMITS, Demo.BANNER)

    val bannerRecurring: String = adUnit(RuStore.BANNER_RECURRING, Demo.BANNER)

    private fun adUnit(rustoreId: String, demoId: String): String = when (BuildConfig.PLATFORM) {
        1 -> if (BuildConfig.DEBUG) demoId else rustoreId
        else -> ""
    }
}
