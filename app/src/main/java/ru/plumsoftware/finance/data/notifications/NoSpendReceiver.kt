package ru.plumsoftware.finance.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.plumsoftware.finance.data.repository.StreakRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import java.time.LocalDate

/** Действие «Сегодня без трат» из напоминания: день засчитывается в стрик (§8.7). */
class NoSpendReceiver : BroadcastReceiver(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val streakRepository: StreakRepository by inject()
    private val notifier: LocalNotifier by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now().toEpochDay()
                var days: Set<Long> = emptySet()
                settingsRepository.update { s ->
                    days = s.noSpendEpochDays + today
                    s.copy(noSpendEpochDays = days)
                }
                streakRepository.calculateAndSave(days.map { LocalDate.ofEpochDay(it) }.toSet())
                notifier.cancel(LocalNotifier.ID_REMINDER)
            } finally {
                pending.finish()
            }
        }
    }
}
