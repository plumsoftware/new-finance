package ru.plumsoftware.finance.data.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.repository.StreakRepository
import ru.plumsoftware.finance.domain.budget.Upcoming
import ru.plumsoftware.finance.domain.repository.RecurringRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.navigation.AppDeepLinks
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Ежедневно во время напоминания (§6.13, §10):
 * - «Коппи ждёт…», если сегодня нет записей (с действием «Сегодня без трат», §8.7);
 * - «Завтра спишется …» для регулярных платежей.
 */
class DailyNotificationsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val settingsRepository: SettingsRepository by inject()
    private val transactionRepository: TransactionRepository by inject()
    private val recurringRepository: RecurringRepository by inject()
    private val streakRepository: StreakRepository by inject()
    private val notifier: LocalNotifier by inject()

    override suspend fun doWork(): Result = runCatching {
        val settings = settingsRepository.settings.first()
        val today = LocalDate.now()
        val ctx = applicationContext

        if (settings.reminderEnabled && today.toEpochDay() !in settings.noSpendEpochDays) {
            val start = DateFmt.startOfDay(today)
            val end = DateFmt.endOfDayExclusive(today) - 1
            val hasToday = transactionRepository.observeByPeriod(start, end).first().isNotEmpty()
            if (!hasToday) {
                val streak = streakRepository.observe().first().currentStreak
                val body = if (streak > 0) ctx.getString(R.string.notif_reminder_streak, streak)
                else ctx.getString(R.string.notif_reminder)
                val noSpend = PendingIntent.getBroadcast(
                    ctx,
                    LocalNotifier.ID_REMINDER,
                    Intent(ctx, NoSpendReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                notifier.post(
                    LocalNotifier.Channel.REMINDERS,
                    LocalNotifier.ID_REMINDER,
                    ctx.getString(R.string.channel_reminders),
                    body,
                    deepLink = AppDeepLinks.addTransaction(),
                    actions = listOf(LocalNotifier.Action(ctx.getString(R.string.notif_action_no_spend), noSpend)),
                )
            }
        }

        if (settings.recurringNotificationsEnabled) {
            val charges = Upcoming.occurrences(recurringRepository.observeAll().first(), today, 1, DateFmt::toLocalDate)
                .filter { it.daysUntil == 1 }
            charges.forEach { c ->
                notifier.post(
                    LocalNotifier.Channel.PAYMENTS,
                    LocalNotifier.ID_PAYMENT_BASE + c.recurringId.toInt(),
                    ctx.getString(R.string.channel_payments),
                    ctx.getString(R.string.notif_payment_tomorrow, c.title, Money.format(c.amountMinor, settings.defaultCurrencyCode)),
                    deepLink = AppDeepLinks.recurring(),
                )
            }
        }
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        private const val WORK_NAME = "daily_notifications"

        /** Перепланировать на выбранное время (минуты от начала суток). */
        fun schedule(context: Context, minuteOfDay: Int, replace: Boolean = false) {
            val now = LocalDateTime.now()
            var target = now.toLocalDate().atStartOfDay().plusMinutes(minuteOfDay.toLong())
            if (!target.isAfter(now)) target = target.plusDays(1)
            val delay = ChronoUnit.MILLIS.between(now, target)
            val request = PeriodicWorkRequestBuilder<DailyNotificationsWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
