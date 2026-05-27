package ru.plumsoftware.finance.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.plumsoftware.finance.data.firebase.InAppMessagingHandler
import ru.plumsoftware.finance.data.firebase.NotificationDisplayHelper
import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.NotificationDao
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.datastore.PushTokenDataStore
import ru.plumsoftware.finance.data.local.datastore.SettingsDataStore
import ru.plumsoftware.finance.data.repository.AnalyticsRepositoryImpl
import ru.plumsoftware.finance.data.repository.CategoryRepositoryImpl
import ru.plumsoftware.finance.data.repository.NotificationRepositoryImpl
import ru.plumsoftware.finance.data.repository.PushMessagingRepositoryImpl
import ru.plumsoftware.finance.data.repository.SettingsRepositoryImpl
import ru.plumsoftware.finance.data.repository.SmartAssetRepositoryImpl
import ru.plumsoftware.finance.data.repository.TransactionRepositoryImpl
import ru.plumsoftware.finance.domain.repository.AnalyticsRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.NotificationRepository
import ru.plumsoftware.finance.domain.repository.PushMessagingRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository

val dataModule = module {
    single { FinanceDatabase.create(androidContext()) }

    single<CategoryDao> { get<FinanceDatabase>().categoryDao() }
    single<TransactionDao> { get<FinanceDatabase>().transactionDao() }
    single<SmartAssetDao> { get<FinanceDatabase>().smartAssetDao() }
    single<NotificationDao> { get<FinanceDatabase>().notificationDao() }

    single { SettingsDataStore(androidContext()) }
    single { PushTokenDataStore(androidContext()) }
    single { NotificationDisplayHelper(androidContext()) }
    single { InAppMessagingHandler(get()) }

    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get()) }
    single<SmartAssetRepository> {
        SmartAssetRepositoryImpl(get(), get(), get())
    }
    single<AnalyticsRepository> { AnalyticsRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<PushMessagingRepository> { PushMessagingRepositoryImpl(get()) }
}
