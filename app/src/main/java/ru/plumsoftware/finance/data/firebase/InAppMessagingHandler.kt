package ru.plumsoftware.finance.data.firebase

import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingImpressionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.repository.NotificationRepository

class InAppMessagingHandler(
    private val notificationRepository: NotificationRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun register() {
        val inAppMessaging = FirebaseInAppMessaging.getInstance()

        inAppMessaging.addImpressionListener(
            FirebaseInAppMessagingImpressionListener { message ->
                scope.launch {
                    val notification = FirebaseNotificationMapper.fromInAppMessage(message)
                    notificationRepository.save(notification)
                }
            },
        )

        inAppMessaging.addClickListener(
            FirebaseInAppMessagingClickListener { _, _ ->
                // Клики обрабатываются Firebase SDK; при необходимости — deep link здесь.
            },
        )
    }
}
