package ru.plumsoftware.finance.data.util

import java.util.Calendar

private const val DAY_MILLIS = 86_400_000L

fun startOfDayMillis(epochMillis: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = epochMillis }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun endOfDayMillis(epochMillis: Long): Long = startOfDayMillis(epochMillis) + DAY_MILLIS

fun isWeekday(epochMillis: Long): Boolean {
    val day = Calendar.getInstance().apply { timeInMillis = epochMillis }
        .get(Calendar.DAY_OF_WEEK)
    return day != Calendar.SATURDAY && day != Calendar.SUNDAY
}
