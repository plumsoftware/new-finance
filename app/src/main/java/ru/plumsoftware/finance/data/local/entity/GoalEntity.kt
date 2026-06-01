package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val targetAmountMinor: Long,
    val savedAmountMinor: Long,
    val colorHex: String,
    val deadline: Long?,
    val note: String?,
    val showOnHome: Boolean,
    val isCompleted: Boolean,
    val createdAtMillis: Long,
)
