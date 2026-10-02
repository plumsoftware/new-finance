package ru.plumsoftware.finance.data.notifications

import android.content.Context
import kotlinx.coroutines.flow.first
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.MonthPeriod
import ru.plumsoftware.finance.domain.model.NotificationSource
import ru.plumsoftware.finance.domain.model.NotificationType
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.NotificationRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.navigation.AppDeepLinks
import ru.plumsoftware.finance.presentation.common.Money

/**
 * Уведомления о лимитах при пересечении 80% и 100% (§8.4, §10): одно на категорию и порог за месяц.
 */
class LimitAlertService(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationRepository: NotificationRepository,
    private val notifier: LocalNotifier,
) {
    private val prefs = context.getSharedPreferences("limit_alerts", Context.MODE_PRIVATE)

    suspend fun check(categoryId: Long?) {
        categoryId ?: return
        val settings = settingsRepository.settings.first()
        if (!settings.limitNotificationsEnabled) return
        val category = categoryRepository.getById(categoryId) ?: return
        if (category.type != CategoryType.EXPENSE) return
        val limit = category.monthlyLimitMinor?.takeIf { it > 0 } ?: return
        val period = MonthPeriod.current()
        val (start, end) = period.toMillisRange()
        val spent = transactionRepository.observeByPeriod(start, end - 1).first()
            .filter { it.type == TransactionType.EXPENSE && it.categoryId == categoryId }
            .sumOf { it.amountMinor }
        val percent = spent * 100 / limit
        val monthKey = "${period.year}${period.month.toString().padStart(2, '0')}"
        val currency = settings.defaultCurrencyCode
        when {
            percent >= 100 -> notifyOnce("${categoryId}_${monthKey}_100") {
                val body = context.getString(R.string.notif_limit_exceeded, category.name, Money.format(spent - limit, currency))
                send(categoryId, NotificationType.LIMIT_EXCEEDED, body)
            }
            percent >= 80 -> notifyOnce("${categoryId}_${monthKey}_80") {
                val body = context.getString(R.string.notif_limit_80, category.name, Money.format(limit - spent, currency))
                send(categoryId, NotificationType.LIMIT_WARNING, body)
            }
        }
    }

    private inline fun notifyOnce(key: String, block: () -> Unit) {
        if (prefs.getBoolean(key, false)) return
        prefs.edit().putBoolean(key, true).apply()
        block()
    }

    private suspend fun send(categoryId: Long, type: NotificationType, body: String) {
        val title = context.getString(R.string.channel_limits)
        notificationRepository.save(
            AppNotification(
                type = type,
                title = title,
                body = body,
                source = NotificationSource.IN_APP,
                relatedCategoryId = categoryId,
                isDisplayed = true,
            ),
        )
        notifier.post(
            LocalNotifier.Channel.LIMITS,
            LocalNotifier.ID_LIMIT_BASE + categoryId.toInt(),
            title,
            body,
            deepLink = AppDeepLinks.limits(),
        )
    }
}
