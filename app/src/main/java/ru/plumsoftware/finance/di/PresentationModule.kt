package ru.plumsoftware.finance.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.plumsoftware.finance.presentation.accounts.AccountEditorViewModel
import ru.plumsoftware.finance.presentation.accounts.AccountsViewModel
import ru.plumsoftware.finance.presentation.addtransaction.AddTransactionViewModel
import ru.plumsoftware.finance.presentation.analytics.AnalyticsViewModel
import ru.plumsoftware.finance.presentation.achievements.AchievementsViewModel
import ru.plumsoftware.finance.presentation.limits.LimitsViewModel
import ru.plumsoftware.finance.presentation.categories.CategoriesViewModel
import ru.plumsoftware.finance.presentation.categories.CategoryEditorViewModel
import ru.plumsoftware.finance.presentation.dashboard.DashboardViewModel
import ru.plumsoftware.finance.presentation.history.HistoryViewModel
import ru.plumsoftware.finance.presentation.goals.CreateGoalViewModel
import ru.plumsoftware.finance.presentation.goals.GoalDetailViewModel
import ru.plumsoftware.finance.presentation.goals.GoalsViewModel
import ru.plumsoftware.finance.presentation.notifications.NotificationsViewModel
import ru.plumsoftware.finance.presentation.onboarding.OnboardingViewModel
import ru.plumsoftware.finance.presentation.export.ExportViewModel
import ru.plumsoftware.finance.presentation.importdata.ImportViewModel
import ru.plumsoftware.finance.presentation.permissions.PermissionsViewModel
import ru.plumsoftware.finance.presentation.recurring.RecurringViewModel
import ru.plumsoftware.finance.presentation.security.AppLockViewModel
import ru.plumsoftware.finance.presentation.settings.SettingsViewModel
import ru.plumsoftware.finance.presentation.smartsavings.CreateSmartSavingsViewModel
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsDetailViewModel
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsViewModel

val presentationModule = module {
    viewModel { OnboardingViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), androidContext()) }
    viewModel { HistoryViewModel(get(), get(), get(), get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { CategoryEditorViewModel(get(), get(), androidContext()) }
    viewModel { AddTransactionViewModel(get(), get(), get(), get(), get(),androidContext()) }
    viewModel { SmartSavingsViewModel(get(), get()) }
    viewModel { GoalsViewModel(get(), get()) }
    viewModel { AchievementsViewModel(get(), get(), get(), get(), get(), androidContext()) }
    viewModel { CreateGoalViewModel(get(), get(), get()) }
    viewModel { (goalId: Long) -> GoalDetailViewModel(goalId, get(), get(), get(), androidContext()) }
    viewModel { CreateSmartSavingsViewModel(get(), get(), get(), androidContext()) }
    viewModel { (assetId: Long) ->
        SmartSavingsDetailViewModel(assetId, get(), get(), androidContext())
    }
    viewModel { AnalyticsViewModel(get(), get(), get(), get(), androidContext()) }
    viewModel { LimitsViewModel(get(), get(), get()) }
    viewModel { NotificationsViewModel(get()) }
    viewModel { AccountsViewModel(get(), get()) }
    viewModel { (accountId: Long?) -> AccountEditorViewModel(accountId, get(), get()) }
    viewModel { SettingsViewModel(get(), get(), androidContext()) }
    viewModel { PermissionsViewModel(get()) }
    viewModel { ExportViewModel(get()) }
    viewModel { ImportViewModel(get(), androidContext()) }
    viewModel { RecurringViewModel(get(), get()) }
    viewModel { AppLockViewModel() }
}
