package ru.plumsoftware.finance.domain.model

data class Goal(
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val targetAmountMinor: Long,
    val savedAmountMinor: Long = 0,
    val colorHex: String,
    val deadline: Long? = null,
    val note: String? = null,
    val showOnHome: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

val Goal.progress: Float
    get() = if (targetAmountMinor <= 0L) 0f else {
        (savedAmountMinor.toFloat() / targetAmountMinor.toFloat()).coerceIn(0f, 1f)
    }

val Goal.remainingMinor: Long
    get() = (targetAmountMinor - savedAmountMinor).coerceAtLeast(0)

val Goal.daysLeft: Int?
    get() = deadline?.let {
        ((it - System.currentTimeMillis()) / 86_400_000L).toInt()
    }

val Goal.isOverdue: Boolean
    get() = deadline != null &&
        System.currentTimeMillis() > deadline &&
        !isCompleted
