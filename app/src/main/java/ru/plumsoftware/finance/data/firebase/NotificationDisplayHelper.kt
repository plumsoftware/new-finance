package ru.plumsoftware.finance.data.firebase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.plumsoftware.finance.MainActivity
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.navigation.AppDeepLinks
import ru.plumsoftware.finance.navigation.parseNotificationDeepLink

class NotificationDisplayHelper(
    private val context: Context,
) {
    fun showSystemNotification(notification: AppNotification, notificationId: Int) {
        if (!canPostNotifications()) return

        val launchIntent = launchIntentForNotification(notification)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(notification.body),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (hasDeepLink(notification)) {
            val deepLinkIntent = launchIntentForNotification(notification)
            val actionPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + ACTION_PENDING_INTENT_OFFSET,
                deepLinkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(
                0,
                context.getString(R.string.notification_action_open),
                actionPendingIntent,
            )
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasDeepLink(notification: AppNotification): Boolean =
        parseNotificationDeepLink(notification.data) != null

    private fun launchIntentForNotification(notification: AppNotification): Intent {
        val deepLinkUri = parseNotificationDeepLink(notification.data)
        val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (deepLinkUri != null) {
            return Intent(Intent.ACTION_VIEW, deepLinkUri, context, MainActivity::class.java).apply {
                this.flags = flags
                putExtra(EXTRA_NOTIFICATION_ID, notification.id)
            }
        }
        return Intent(context, MainActivity::class.java).apply {
            this.flags = flags
            putExtra(EXTRA_NOTIFICATION_ID, notification.id)
        }
    }

    companion object {
        const val CHANNEL_ID = "finance_push"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val DATA_KEY_DEEP_LINK = "deep_link"
        private const val ACTION_PENDING_INTENT_OFFSET = 100_000
    }
}
