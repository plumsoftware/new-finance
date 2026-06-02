package ru.plumsoftware.finance.data.firebase

import com.google.firebase.inappmessaging.model.BannerMessage
import com.google.firebase.inappmessaging.model.CardMessage
import com.google.firebase.inappmessaging.model.ImageOnlyMessage
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.inappmessaging.model.ModalMessage
import android.content.Intent
import com.google.firebase.messaging.RemoteMessage
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.NotificationSource

object FirebaseNotificationMapper {

    fun fromPersistIntent(intent: Intent, fallbackTitle: String): AppNotification {
        val data = mutableMapOf<String, String>()
        intent.extras?.let { bundle -> extractDataFromBundle(bundle, data) }

        val title = intent.getStringExtra(PushNotificationPersistService.EXTRA_TITLE)
            ?: data["title"]
            ?: data["gcm.notification.title"]
            ?: fallbackTitle
        val body = intent.getStringExtra(PushNotificationPersistService.EXTRA_BODY)
            ?: data["body"]
            ?: data["gcm.notification.body"]
            ?: ""

        return AppNotification(
            title = title,
            body = body,
            imageUrl = intent.getStringExtra(PushNotificationPersistService.EXTRA_IMAGE_URL)
                ?: data["image"]
                ?: data["imageUrl"],
            source = NotificationSource.FCM,
            messageId = intent.getStringExtra(PushNotificationPersistService.EXTRA_MESSAGE_ID)
                ?: intent.extras?.getString("google.message_id")
                ?: data["messageId"],
            campaignId = intent.getStringExtra(PushNotificationPersistService.EXTRA_CAMPAIGN_ID)
                ?: data["campaignId"],
            data = data,
            receivedAtMillis = System.currentTimeMillis(),
            isRead = false,
            isDisplayed = false,
        )
    }

    fun fromRemoteMessage(
        message: RemoteMessage,
        fallbackTitle: String,
    ): AppNotification {
        val notification = message.notification
        val title = notification?.title
            ?: message.data["title"]
            ?: message.data["gcm.notification.title"]
            ?: fallbackTitle
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

    fun fromInAppMessage(message: InAppMessage, fallbackInAppTitle: String): AppNotification {
        val (title, body) = extractInAppText(message, fallbackInAppTitle)
        return AppNotification(
            title = title,
            body = body,
            imageUrl = null,
            source = NotificationSource.IN_APP,
            messageId = message.campaignMetadata?.campaignId,
            campaignId = message.campaignMetadata?.campaignId,
            data = mapOf(
                "campaignName" to (message.campaignMetadata?.campaignName.orEmpty()),
                "messageType" to message.messageType?.name.orEmpty(),
            ),
            receivedAtMillis = System.currentTimeMillis(),
            isRead = false,
            isDisplayed = true,
        )
    }

    private fun extractDataFromBundle(
        bundle: android.os.Bundle,
        data: MutableMap<String, String>,
    ) {
        bundle.keySet().forEach { key ->
            when {
                key.startsWith(PushNotificationPersistService.EXTRA_DATA_PREFIX) ->
                    data[key.removePrefix(PushNotificationPersistService.EXTRA_DATA_PREFIX)] =
                        bundle.getString(key).orEmpty()
                !key.startsWith("extra_") &&
                    !key.startsWith("google.") &&
                    !key.startsWith("gcm.") &&
                    key != "from" &&
                    key != "collapse_key" -> {
                    bundle.getString(key)?.let { data[key] = it }
                }
            }
        }
    }

    private fun extractInAppText(
        message: InAppMessage,
        fallbackInAppTitle: String,
    ): Pair<String, String> {
        val campaignName = message.campaignMetadata?.campaignName
        return when (message) {
            is ModalMessage -> {
                val title = message.title.text ?: campaignName ?: fallbackInAppTitle
                val body = message.body?.text.orEmpty()
                title to body
            }
            is BannerMessage -> {
                val title = message.title.text ?: campaignName ?: fallbackInAppTitle
                val body = message.body?.text.orEmpty()
                title to body
            }
            is CardMessage -> {
                val title = message.title.text ?: campaignName ?: fallbackInAppTitle
                val body = message.body?.text.orEmpty()
                title to body
            }
            is ImageOnlyMessage -> {
                (campaignName ?: fallbackInAppTitle) to ""
            }
            else -> (campaignName ?: fallbackInAppTitle) to ""
        }
    }
}
