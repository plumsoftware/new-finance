package ru.plumsoftware.finance

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.plumsoftware.finance.BuildConfig
import ru.plumsoftware.finance.data.firebase.InAppMessagingHandler
import ru.plumsoftware.finance.data.firebase.NotificationDisplayHelper
import ru.plumsoftware.finance.data.work.RecurringTransactionWorker
import ru.plumsoftware.finance.data.notifications.DailyNotificationsWorker
import ru.plumsoftware.finance.data.notifications.LocalNotifier
import ru.plumsoftware.finance.data.config.CalcConfigRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import com.yandex.mobile.ads.common.YandexAds
// import ru.plumsoftware.finance.ads.InterstitialAdManager
// import ru.plumsoftware.finance.ads.InterstitialPlacement
import ru.plumsoftware.finance.di.adsModule
import ru.plumsoftware.finance.di.dataModule
import ru.plumsoftware.finance.di.presentationModule
import ru.plumsoftware.finance.domain.repository.PushMessagingRepository

class FinanceApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FinanceApplication)
            modules(dataModule, presentationModule, adsModule)
        }

        YandexAds.initialize(this) {
            // getKoin().get<InterstitialAdManager>()
            //     .preload(InterstitialPlacement.TRANSACTION.adUnitId())
        }

        getKoin().get<NotificationDisplayHelper>().createNotificationChannel()

        if (BuildConfig.FIREBASE_ENABLED) {
            getKoin().get<InAppMessagingHandler>().register()
            appScope.launch {
                getKoin().get<PushMessagingRepository>().refreshFcmToken()
            }
        }

        RecurringTransactionWorker.schedule(this)

        getKoin().get<LocalNotifier>().createChannels()
        getKoin().get<CalcConfigRepository>().refresh()
        appScope.launch {
            val minute = getKoin().get<SettingsRepository>().settings.first().reminderMinuteOfDay
            DailyNotificationsWorker.schedule(this@FinanceApplication, minute)
        }
    }
}
