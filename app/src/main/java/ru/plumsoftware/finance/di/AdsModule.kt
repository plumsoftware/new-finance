package ru.plumsoftware.finance.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.plumsoftware.finance.ads.InterstitialAdManager

val adsModule = module {
    single { InterstitialAdManager(androidApplication()) }
}
