package ru.plumsoftware.finance.data.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.plumsoftware.finance.domain.repository.PushMessagingRepository

class FinanceMessagingService : FirebaseMessagingService() {

    private val pushMessagingRepository: PushMessagingRepository by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        PushNotificationPersistService.enqueue(this, message)
    }

    override fun onNewToken(token: String) {
        serviceScope.launch {
            pushMessagingRepository.saveFcmToken(token)
        }
    }
}
