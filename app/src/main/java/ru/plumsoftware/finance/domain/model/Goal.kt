package ru.plumsoftware.finance.domain.model

import kotlin.math.ceil
import kotlin.math.max

data class Goal(
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
    val currencyCode: String = "RUB",
    val accountId: Long? = null,
)

val Goal.progress: Float
    get() = if (targetAmountMinor <= 0L) 0f else {
        (savedAmountMinor.toFloat() / targetAmountMinor).coerceIn(0f, 1f)
    }

val Goal.remainingMinor: Long
    get() = max(0L, targetAmountMinor - savedAmountMinor)

val Goal.daysLeft: Int?
    get() {
        val deadlineMillis = deadline ?: return null
        val now = System.currentTimeMillis()
        if (deadlineMillis <= now) return 0
        val diffDays = ceil((deadlineMillis - now).toDouble() / 86_400_000.0).toInt()
        return diffDays.coerceAtLeast(0)
    }

val Goal.isOverdue: Boolean
    get() {
        val deadlineMillis = deadline ?: return false
        return !isCompleted && deadlineMillis < System.currentTimeMillis()
    }
