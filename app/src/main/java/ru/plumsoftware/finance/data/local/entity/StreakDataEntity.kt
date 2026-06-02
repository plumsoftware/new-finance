package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_data")
data class StreakDataEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long = 0L,
    val todayHasActivity: Boolean = false,
    val totalUnlocked: Int = 0,
)
