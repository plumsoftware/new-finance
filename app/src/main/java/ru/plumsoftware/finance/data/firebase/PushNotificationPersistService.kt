package ru.plumsoftware.finance.data.firebase

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.NotificationSource
import ru.plumsoftware.finance.domain.repository.NotificationRepository
import ru.plumsoftware.finance.util.isAppInForeground

/**
 * Saves incoming FCM messages to Room and shows a local notification.
 * Started from [FinanceMessagingService] and when the app opens a system FCM notification.
 */
class PushNotificationPersistService : Service() {

    private val notificationRepository: NotificationRepository by inject()
    private val notificationDisplayHelper: NotificationDisplayHelper by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        serviceScope.launch {
            try {
                persistFromIntent(intent)
            } finally {
                stopSelf(startId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun persistFromIntent(intent: Intent) {
        val appNotification = FirebaseNotificationMapper.fromPersistIntent(
            intent = intent,
            fallbackTitle = getString(R.string.notification_fallback_title),
        )
        val messageId = appNotification.messageId
        if (messageId != null &&
            notificationRepository.existsByMessageId(messageId, NotificationSource.FCM)
        ) {
            return
        }

        val savedId = notificationRepository.save(appNotification)
        if (savedId <= 0L) return

        val showTray = intent.getBooleanExtra(EXTRA_SHOW_TRAY, false)
        if (!showTray) {
            notificationRepository.markAsDisplayed(savedId)
            return
        }

        val savedNotification = appNotification.copy(id = savedId)
        notificationDisplayHelper.showSystemNotification(
            notification = savedNotification,
            notificationId = savedId.toInt(),
        )
        notificationRepository.markAsDisplayed(savedId)
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_CAMPAIGN_ID = "extra_campaign_id"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_DATA_PREFIX = "extra_data_"
        private const val EXTRA_SHOW_TRAY = "extra_show_tray"

        private val FCM_META_KEYS = setOf(
            "google.message_id",
            "google.c.sender.id",
            "google.c.a.e",
            "google.c.a.c_l",
            "google.c.a.ts",
            "google.c.a.udt",
            "google.c.a.m_c",
            "google.c.a.m_l",
            "from",
            "collapse_key",
            "gcm.notification.e",
            NotificationDisplayHelper.EXTRA_NOTIFICATION_ID,
        )

        fun enqueue(context: Context, message: RemoteMessage) {
            val intent = Intent(context, PushNotificationPersistService::class.java)
            val notification = message.notification
            intent.putExtra(
                EXTRA_TITLE,
                notification?.title ?: message.data["title"],
            )
            intent.putExtra(
                EXTRA_BODY,
                notification?.body ?: message.data["body"],
            )
            intent.putExtra(EXTRA_MESSAGE_ID, message.messageId ?: message.data["messageId"])
            intent.putExtra(EXTRA_CAMPAIGN_ID, message.data["campaignId"])
            intent.putExtra(
                EXTRA_IMAGE_URL,
                notification?.imageUrl?.toString()
                    ?: message.data["image"]
                    ?: message.data["imageUrl"],
            )
            message.data.forEach { (key, value) ->
                intent.putExtra(EXTRA_DATA_PREFIX + key, value)
            }
            intent.putExtra(
                EXTRA_SHOW_TRAY,
                shouldShowTrayForFcmMessage(context, message),
            )
            context.startService(intent)
        }

        /**
         * Persists a push opened from the **system** FCM tray (not our local notification).
         */
        fun enqueueFromTapIfNeeded(context: Context, intent: Intent) {
            val extras = intent.extras ?: return
            val messageId = extras.getString("google.message_id") ?: return

            val persistIntent = Intent(context, PushNotificationPersistService::class.java)
            persistIntent.putExtra(EXTRA_SHOW_TRAY, false)
            persistIntent.putExtra(
                EXTRA_TITLE,
                extras.getString("title")
                    ?: extras.getString("gcm.notification.title"),
            )
            persistIntent.putExtra(
                EXTRA_BODY,
                extras.getString("body")
                    ?: extras.getString("gcm.notification.body"),
            )
            persistIntent.putExtra(EXTRA_MESSAGE_ID, messageId)
            persistIntent.putExtra(EXTRA_CAMPAIGN_ID, extras.getString("campaignId"))
            persistIntent.putExtra(EXTRA_IMAGE_URL, extras.getString("image"))

            extras.keySet()
                .filter { key -> !isMetaKey(key) }
                .forEach { key ->
                    extras.getString(key)?.let { value ->
                        persistIntent.putExtra(EXTRA_DATA_PREFIX + key, value)
                    }
                }

            context.startService(persistIntent)
        }

        private fun shouldShowTrayForFcmMessage(
            context: Context,
            message: RemoteMessage,
        ): Boolean {
            if (message.notification == null) return true
            return isAppInForeground(context)
        }

        private fun isMetaKey(key: String): Boolean =
            key in FCM_META_KEYS || key.startsWith("google.") || key.startsWith("gcm.")
    }
}
