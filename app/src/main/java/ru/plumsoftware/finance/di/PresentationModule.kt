package ru.plumsoftware.finance.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.plumsoftware.finance.presentation.addtransaction.AddTransactionViewModel
import ru.plumsoftware.finance.presentation.analytics.AnalyticsViewModel
import ru.plumsoftware.finance.presentation.limits.LimitsViewModel
import ru.plumsoftware.finance.presentation.categories.CategoriesViewModel
import ru.plumsoftware.finance.presentation.categories.CategoryEditorViewModel
import ru.plumsoftware.finance.presentation.dashboard.DashboardViewModel
import ru.plumsoftware.finance.presentation.history.HistoryViewModel
import ru.plumsoftware.finance.presentation.notifications.NotificationsViewModel
import ru.plumsoftware.finance.presentation.onboarding.OnboardingViewModel
import ru.plumsoftware.finance.presentation.export.ExportViewModel
import ru.plumsoftware.finance.presentation.permissions.PermissionsViewModel
import ru.plumsoftware.finance.presentation.recurring.RecurringViewModel
import ru.plumsoftware.finance.presentation.security.AppLockViewModel
import ru.plumsoftware.finance.presentation.settings.SettingsViewModel
import ru.plumsoftware.finance.presentation.smartsavings.CreateSmartSavingsViewModel
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsDetailViewModel
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsViewModel

val presentationModule = module {
    viewModel { OnboardingViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { CategoryEditorViewModel(get(), get(), androidContext()) }
    viewModel { AddTransactionViewModel(get(), get(), get(), androidContext()) }
    viewModel { SmartSavingsViewModel(get(), get()) }
    viewModel { CreateSmartSavingsViewModel(get(), get(), get()) }
    viewModel { (assetId: Long) ->
        SmartSavingsDetailViewModel(assetId, get(), get())
    }
    viewModel { AnalyticsViewModel(get(), get(), get(), get()) }
    viewModel { LimitsViewModel(get(), get(), get()) }
    viewModel { NotificationsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), androidContext()) }
    viewModel { PermissionsViewModel(get()) }
    viewModel { ExportViewModel(get(), androidContext()) }
    viewModel { RecurringViewModel(get(), get()) }
    viewModel { AppLockViewModel() }
}
