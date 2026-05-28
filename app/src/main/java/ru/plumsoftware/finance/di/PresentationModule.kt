package ru.plumsoftware.finance.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.plumsoftware.finance.presentation.addtransaction.AddTransactionViewModel
import ru.plumsoftware.finance.presentation.analytics.AnalyticsViewModel
import ru.plumsoftware.finance.presentation.dashboard.DashboardViewModel
import ru.plumsoftware.finance.presentation.history.HistoryViewModel
import ru.plumsoftware.finance.presentation.onboarding.OnboardingViewModel
import ru.plumsoftware.finance.presentation.settings.SettingsViewModel
import ru.plumsoftware.finance.presentation.smartsavings.CreateSmartSavingsViewModel
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsDetailViewModel
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsViewModel

val presentationModule = module {
    viewModel { OnboardingViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { AddTransactionViewModel(get(), get(), get()) }
    viewModel { SmartSavingsViewModel(get(), get()) }
    viewModel { CreateSmartSavingsViewModel(get(), get(), get()) }
    viewModel { (assetId: Long) ->
        SmartSavingsDetailViewModel(assetId, get(), get())
    }
    viewModel { AnalyticsViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), androidContext()) }
}
