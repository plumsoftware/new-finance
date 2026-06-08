package ru.plumsoftware.finance.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.plumsoftware.finance.data.firebase.InAppMessagingHandler
import ru.plumsoftware.finance.data.firebase.NotificationDisplayHelper
import ru.plumsoftware.finance.data.local.dao.AccountDao
import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.GoalDao
import ru.plumsoftware.finance.data.local.dao.NotificationDao
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.RecurringTransactionDao
import ru.plumsoftware.finance.data.local.dao.StreakDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.datastore.PushTokenDataStore
import ru.plumsoftware.finance.data.local.datastore.SettingsDataStore
import ru.plumsoftware.finance.data.repository.AccountRepositoryImpl
import ru.plumsoftware.finance.data.repository.AnalyticsRepositoryImpl
import ru.plumsoftware.finance.data.repository.CategoryRepositoryImpl
import ru.plumsoftware.finance.data.repository.BackupRepositoryImpl
import ru.plumsoftware.finance.data.repository.GoalRepositoryImpl
import ru.plumsoftware.finance.data.repository.NotificationRepositoryImpl
import ru.plumsoftware.finance.data.repository.PermissionsRepositoryImpl
import ru.plumsoftware.finance.data.repository.PushMessagingRepositoryImpl
import ru.plumsoftware.finance.data.repository.RecurringRepositoryImpl
import ru.plumsoftware.finance.data.repository.SettingsRepositoryImpl
import ru.plumsoftware.finance.data.repository.SmartAssetRepositoryImpl
import ru.plumsoftware.finance.data.repository.StreakRepository
import ru.plumsoftware.finance.data.repository.TransactionRepositoryImpl
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.AnalyticsRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.BackupRepository
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.NotificationRepository
import ru.plumsoftware.finance.domain.repository.PermissionsRepository
import ru.plumsoftware.finance.domain.repository.PushMessagingRepository
import ru.plumsoftware.finance.domain.repository.RecurringRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.domain.insights.InsightsEngine
import ru.plumsoftware.finance.domain.notifications.LimitNotificationsEngine

val dataModule = module {
    single { FinanceDatabase.create(androidContext()) }

    single<AccountDao> { get<FinanceDatabase>().accountDao() }
    single<CategoryDao> { get<FinanceDatabase>().categoryDao() }
    single<TransactionDao> { get<FinanceDatabase>().transactionDao() }
    single<SmartAssetDao> { get<FinanceDatabase>().smartAssetDao() }
    single<GoalDao> { get<FinanceDatabase>().goalDao() }
    single<StreakDao> { get<FinanceDatabase>().streakDao() }
    single<AchievementUnlockDao> { get<FinanceDatabase>().achievementUnlockDao() }
    single<NotificationDao> { get<FinanceDatabase>().notificationDao() }
    single<RecurringTransactionDao> { get<FinanceDatabase>().recurringTransactionDao() }

    single { SettingsDataStore(androidContext()) }
    single { PushTokenDataStore(androidContext()) }
    single { NotificationDisplayHelper(androidContext()) }
    single { InAppMessagingHandler(get(), androidContext()) }

    single<AccountRepository> { AccountRepositoryImpl(get(), get(), androidContext()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }
    single<SmartAssetRepository> {
        SmartAssetRepositoryImpl(get(), get(), get())
    }
    single<GoalRepository> { GoalRepositoryImpl(get(), get()) }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get(), get(), get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single { StreakRepository(get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<PushMessagingRepository> { PushMessagingRepositoryImpl(get()) }
    single<BackupRepository> {
        BackupRepositoryImpl(androidContext(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    single<RecurringRepository> { RecurringRepositoryImpl(get(), get()) }
    single<PermissionsRepository> { PermissionsRepositoryImpl(androidContext()) }
    single { InsightsEngine() }
    single { LimitNotificationsEngine(get()) }
}
