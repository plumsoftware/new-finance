package ru.plumsoftware.finance.domain.model

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long = 0L,
    val todayHasActivity: Boolean = false,
    val totalUnlocked: Int = 0,
)
