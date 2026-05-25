package ru.plumsoftware.finance.data.firebase

import com.google.firebase.inappmessaging.model.BannerMessage
import com.google.firebase.inappmessaging.model.CardMessage
import com.google.firebase.inappmessaging.model.ImageOnlyMessage
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.inappmessaging.model.ModalMessage
import com.google.firebase.messaging.RemoteMessage
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.NotificationSource

object FirebaseNotificationMapper {

    fun fromRemoteMessage(message: RemoteMessage): AppNotification {
        val notification = message.notification
        val title = notification?.title
            ?: message.data["title"]
            ?: message.data["gcm.notification.title"]
            ?: "Уведомление"
        val body = notification?.body
            ?: message.data["body"]
            ?: message.data["gcm.notification.body"]
            ?: ""
        val imageUrl = notification?.imageUrl?.toString()
            ?: message.data["image"]
            ?: message.data["imageUrl"]

        return AppNotification(
            title = title,
            body = body,
            imageUrl = imageUrl,
            source = NotificationSource.FCM,
            messageId = message.messageId ?: message.data["messageId"],
            campaignId = message.data["campaignId"],
            data = message.data,
            receivedAtMillis = System.currentTimeMillis(),
            isRead = false,
            isDisplayed = false,
        )
    }

    fun fromInAppMessage(message: InAppMessage): AppNotification {
        val (title, body) = extractInAppText(message)
        return AppNotification(
            title = title,
            body = body,
            imageUrl = null,
            source = NotificationSource.IN_APP,
            messageId = message.campaignMetadata?.campaignId,
            campaignId = message.campaignMetadata?.campaignId,
            data = mapOf(
                "campaignName" to (message.campaignMetadata?.campaignName.orEmpty()),
                "messageType" to message.messageType.name,
            ),
            receivedAtMillis = System.currentTimeMillis(),
            isRead = false,
            isDisplayed = true,
        )
    }

    private fun extractInAppText(message: InAppMessage): Pair<String, String> {
        val campaignName = message.campaignMetadata?.campaignName
        return when (message) {
            is ModalMessage -> {
                val title = message.title.text ?: campaignName ?: "In-App"
                val body = message.body?.text.orEmpty()
                title to body
            }
            is BannerMessage -> {
                val title = message.title.text ?: campaignName ?: "In-App"
                val body = message.body?.text.orEmpty()
                title to body
            }
            is CardMessage -> {
                val title = message.title.text ?: campaignName ?: "In-App"
                val body = message.body?.text.orEmpty()
                title to body
            }
            is ImageOnlyMessage -> {
                (campaignName ?: "In-App") to ""
            }
            else -> (campaignName ?: "In-App") to ""
        }
    }
}
