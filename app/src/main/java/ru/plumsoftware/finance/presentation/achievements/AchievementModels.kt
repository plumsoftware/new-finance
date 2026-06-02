package ru.plumsoftware.finance.presentation.achievements

import ru.plumsoftware.finance.domain.model.StreakData

data class Achievement(
    val key: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlockedAtMillis: Long? = null,
    val progress: Float? = null,
    val progressLabel: String? = null,
)

data class AchievementsUiState(
    val streak: StreakData = StreakData(),
    val unlocked: List<Achievement> = emptyList(),
    val locked: List<Achievement> = emptyList(),
)

object AchievementKeys {
    const val TOTAL_COUNT = 12

    const val FIRST_TX = "first_tx"
    const val STREAK_7 = "streak_7"
    const val STREAK_30 = "streak_30"
    const val STREAK_100 = "streak_100"
    const val TX_10 = "tx_10"
    const val TX_50 = "tx_50"
    const val TX_100 = "tx_100"
    const val FIRST_GOAL = "first_goal"
    const val GOAL_DONE = "goal_done"
    const val FIRST_SAVING = "first_saving"
    const val PAID_OFF = "paid_off"
    const val POSITIVE_MONTH = "positive_month"
}
