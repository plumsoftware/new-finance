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

    private const val APP_PACKAGE = "ru.plumsoftware.finance"

    /**
     * Ссылка на страницу приложения в магазине (для «Оценить приложение»).
     * Зависит от [BuildConfig.PLATFORM]: 1 — RuStore, 2 — Google Play, 3 — Huawei AppGallery.
     */
    val storeListingUrl: String = when (BuildConfig.PLATFORM) {
        1 -> RuStore.STORE_LISTING
        2 -> ""
        3 -> HuaweiAppStore.STORE_LISTING
        else -> RuStore.STORE_LISTING
    }

    /** Минимальный интервал между показами любых межстраничных объявлений. */
    const val INTERSTITIAL_COOLDOWN_MS: Long = 3 * 60 * 1000L

    private object Demo {
        const val INTERSTITIAL = "demo-interstitial-yandex"
        const val BANNER = "demo-banner-yandex"
        const val NATIVE = "demo-native-content-yandex"
    }

    private object RuStore {
        const val STORE_LISTING = "https://www.rustore.ru/catalog/app/$APP_PACKAGE"
        const val BANNER_CATEGORIES = "R-M-19374501-1"
        const val BANNER_GOALS = "R-M-19374501-2"
        const val BANNER_LIMITS = "R-M-19374501-3"
        const val BANNER_RECURRING = "R-M-19374501-4"
        const val INTERSTITIAL_TRANSACTION = "R-M-19374501-5"
        const val INTERSTITIAL_GOAL = "R-M-19374501-6"
        const val INTERSTITIAL_SMART_SAVINGS = "R-M-19374501-7"
        const val NATIVE_HOME = "R-M-19374501-8"
    }

    private object HuaweiAppStore {
        const val STORE_LISTING = "https://appgallery.huawei.com/#/search/$APP_PACKAGE"
        const val BANNER_CATEGORIES = "R-M-19390613-7"
        const val BANNER_GOALS = "R-M-19390613-6"
        const val BANNER_LIMITS = "R-M-19390613-5"
        const val BANNER_RECURRING = "R-M-19390613-4"
        const val INTERSTITIAL_TRANSACTION = "R-M-19390613-3"
        const val INTERSTITIAL_GOAL = "R-M-19390613-2"
        const val INTERSTITIAL_SMART_SAVINGS = "R-M-19390613-1"
        const val NATIVE_HOME = "R-M-19390613-8"
    }

    val interstitialAfterCreateTransaction: String = adUnit(
        rustoreId = RuStore.INTERSTITIAL_TRANSACTION,
        huaweiId = HuaweiAppStore.INTERSTITIAL_TRANSACTION,
        demoId = Demo.INTERSTITIAL,
    )

    val interstitialAfterCreateGoal: String = adUnit(
        rustoreId = RuStore.INTERSTITIAL_GOAL,
        huaweiId = HuaweiAppStore.INTERSTITIAL_GOAL,
        demoId = Demo.INTERSTITIAL,
    )

    val interstitialAfterCreateSmartSavings: String = adUnit(
        rustoreId = RuStore.INTERSTITIAL_SMART_SAVINGS,
        huaweiId = HuaweiAppStore.INTERSTITIAL_SMART_SAVINGS,
        demoId = Demo.INTERSTITIAL,
    )

    val bannerCategories: String = adUnit(
        rustoreId = RuStore.BANNER_CATEGORIES,
        huaweiId = HuaweiAppStore.BANNER_CATEGORIES,
        demoId = Demo.BANNER,
    )

    val bannerGoals: String = adUnit(
        rustoreId = RuStore.BANNER_GOALS,
        huaweiId = HuaweiAppStore.BANNER_GOALS,
        demoId = Demo.BANNER,
    )

    val bannerLimits: String = adUnit(
        rustoreId = RuStore.BANNER_LIMITS,
        huaweiId = HuaweiAppStore.BANNER_LIMITS,
        demoId = Demo.BANNER,
    )

    val bannerRecurring: String = adUnit(
        rustoreId = RuStore.BANNER_RECURRING,
        huaweiId = HuaweiAppStore.BANNER_RECURRING,
        demoId = Demo.BANNER,
    )

    val nativeHome: String = adUnit(
        rustoreId = RuStore.NATIVE_HOME,
        huaweiId = HuaweiAppStore.NATIVE_HOME,
        demoId = Demo.NATIVE,
    )

    private fun adUnit(rustoreId: String, huaweiId: String, demoId: String): String =
        when (BuildConfig.PLATFORM) {
            1 -> if (BuildConfig.DEBUG) demoId else rustoreId
            3 -> if (BuildConfig.DEBUG) demoId else huaweiId
            else -> ""
        }
}
