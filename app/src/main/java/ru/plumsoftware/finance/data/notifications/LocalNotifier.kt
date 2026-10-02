package ru.plumsoftware.finance.data.notifications

import android.Manifest
import android.annotation.SuppressLint
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

/**
 * Локальные уведомления (ТЗ §10). Каналы «Напоминания», «Лимиты», «Платежи» отключаются отдельно.
 */
class LocalNotifier(private val context: Context) {

    enum class Channel(val id: String, val nameRes: Int, val descRes: Int) {
        REMINDERS("reminders", R.string.channel_reminders, R.string.channel_reminders_desc),
        LIMITS("limits", R.string.channel_limits, R.string.channel_limits_desc),
        PAYMENTS("payments", R.string.channel_payments, R.string.channel_payments_desc),
    }

    data class Action(val title: String, val intent: PendingIntent)

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        Channel.entries.forEach { ch ->
            manager.createNotificationChannel(
                NotificationChannel(ch.id, context.getString(ch.nameRes), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = context.getString(ch.descRes)
                },
            )
        }
    }

    fun canPost(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Разрешение проверяется в [canPost]. */
    @SuppressLint("MissingPermission")
    fun post(
        channel: Channel,
        id: Int,
        title: String,
        body: String,
        deepLink: Uri? = null,
        actions: List<Action> = emptyList(),
    ) {
        if (!canPost()) return
        val intent = (if (deepLink != null) Intent(Intent.ACTION_VIEW, deepLink, context, MainActivity::class.java)
        else Intent(context, MainActivity::class.java)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
        actions.forEach { builder.addAction(0, it.title, it.intent) }
        runCatching { NotificationManagerCompat.from(context).notify(id, builder.build()) }
    }

    fun cancel(id: Int) = NotificationManagerCompat.from(context).cancel(id)

    companion object {
        const val ID_REMINDER = 7_001
        const val ID_LIMIT_BASE = 7_100_000
        const val ID_PAYMENT_BASE = 7_200_000
    }
}
