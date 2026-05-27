package ru.plumsoftware.finance.presentation.common

import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import java.util.Calendar

enum class StatsPeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    CUSTOM,
}

data class PeriodRange(
    val startMillis: Long,
    val endMillis: Long,
)

fun StatsPeriod.resolveRange(
    nowMillis: Long = System.currentTimeMillis(),
    customStartMillis: Long? = null,
    customEndMillis: Long? = null,
): PeriodRange {
    val calendar = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return when (this) {
        StatsPeriod.DAY -> PeriodRange(
            startMillis = startOfDayMillis(nowMillis),
            endMillis = endOfDayMillis(nowMillis),
        )
        StatsPeriod.WEEK -> {
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val start = startOfDayMillis(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            PeriodRange(start, calendar.timeInMillis)
        }
        StatsPeriod.MONTH -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val start = startOfDayMillis(calendar.timeInMillis)
            calendar.add(Calendar.MONTH, 1)
            PeriodRange(start, calendar.timeInMillis)
        }
        StatsPeriod.YEAR -> {
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            val start = startOfDayMillis(calendar.timeInMillis)
            calendar.add(Calendar.YEAR, 1)
            PeriodRange(start, calendar.timeInMillis)
        }
        StatsPeriod.CUSTOM -> {
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            PeriodRange(
                startMillis = customStartMillis ?: startOfDayMillis(calendar.timeInMillis),
                endMillis = customEndMillis ?: endOfDayMillis(nowMillis),
            )
        }
    }
}
