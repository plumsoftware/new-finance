package ru.plumsoftware.finance.data.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.repository.NotificationRepository
import ru.plumsoftware.finance.domain.repository.PushMessagingRepository

class FinanceMessagingService : FirebaseMessagingService() {

    private val notificationRepository: NotificationRepository by inject()
    private val pushMessagingRepository: PushMessagingRepository by inject()
    private val notificationDisplayHelper: NotificationDisplayHelper by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        serviceScope.launch {
            val appNotification = FirebaseNotificationMapper.fromRemoteMessage(
                message = message,
                fallbackTitle = getString(R.string.notification_fallback_title),
            )
            val savedId = notificationRepository.save(appNotification)
            if (savedId <= 0L) return@launch

            val savedNotification = appNotification.copy(id = savedId)
            notificationDisplayHelper.showSystemNotification(
                notification = savedNotification,
                notificationId = savedId.toInt(),
            )
            notificationRepository.markAsDisplayed(savedId)
        }
    }

    override fun onNewToken(token: String) {
        serviceScope.launch {
            pushMessagingRepository.saveFcmToken(token)
        }
    }
}
