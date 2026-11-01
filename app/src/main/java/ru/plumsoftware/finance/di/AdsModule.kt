package ru.plumsoftware.finance.di

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.ads.AppOpenAdManager
import ru.plumsoftware.finance.ads.InterstitialAdManager
import ru.plumsoftware.finance.domain.repository.SettingsRepository

val adsModule = module {
    single { InterstitialAdManager(androidApplication()) }
    single {
        val settingsRepository: SettingsRepository = get()
        AppOpenAdManager(
            application = androidApplication(),
            adUnitId = AppConfig.appOpenAds,
            shouldShow = {
                runBlocking { settingsRepository.settings.first().onboardingCompleted }
            },
        )
    }
}
