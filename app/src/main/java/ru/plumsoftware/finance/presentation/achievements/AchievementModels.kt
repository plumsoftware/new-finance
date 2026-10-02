package ru.plumsoftware.finance.presentation.achievements

import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.achievements.AchievementId
import ru.plumsoftware.finance.domain.model.StreakData

data class Achievement(
    val id: AchievementId,
    val title: String,
    val description: String,
    val emoji: String,
    val value: Int,
    val unlockedAtMillis: Long? = null,
) {
    val key: String get() = id.key
    val isUnlocked: Boolean get() = unlockedAtMillis != null
    val progress: Float get() = if (isUnlocked) 1f else (value.toFloat() / id.target).coerceIn(0f, 1f)
}

data class AchievementsUiState(
    val streak: StreakData = StreakData(),
    val achievements: List<Achievement> = emptyList(),
) {
    val unlockedCount: Int get() = achievements.count { it.isUnlocked }
}

object AchievementKeys {
    const val TOTAL_COUNT = 12
}

/** Эмодзи и строки достижений (§6.12). */
object AchievementUi {
    fun emoji(id: AchievementId): String = when (id) {
        AchievementId.FIRST_STEPS -> "👣"
        AchievementId.WEEK_MARATHON -> "🚀"
        AchievementId.GETTING_INTO -> "🧾"
        AchievementId.MONTH_DISCIPLINE -> "🏆"
        AchievementId.DIARY -> "📒"
        AchievementId.DREAM_CAME_TRUE -> "🥳"
        AchievementId.ECONOMIST -> "🌱"
        AchievementId.WITHIN_LIMITS -> "🧭"
        AchievementId.PIGGY_BANK -> "🐷"
        AchievementId.ANALYST -> "📊"
        AchievementId.PAPERLESS -> "📷"
        AchievementId.LEGEND -> "🌟"
    }

    fun titleRes(id: AchievementId): Int = when (id) {
        AchievementId.FIRST_STEPS -> R.string.ach_first_tx_title
        AchievementId.WEEK_MARATHON -> R.string.ach_streak_7_title
        AchievementId.GETTING_INTO -> R.string.ach_tx_10_title
        AchievementId.MONTH_DISCIPLINE -> R.string.ach_streak_30_title
        AchievementId.DIARY -> R.string.ach_tx_50_title
        AchievementId.DREAM_CAME_TRUE -> R.string.ach_goal_done_title
        AchievementId.ECONOMIST -> R.string.ach_economist_title
        AchievementId.WITHIN_LIMITS -> R.string.ach_within_limits_title
        AchievementId.PIGGY_BANK -> R.string.ach_piggy_title
        AchievementId.ANALYST -> R.string.ach_analyst_title
        AchievementId.PAPERLESS -> R.string.ach_paperless_title
        AchievementId.LEGEND -> R.string.ach_streak_100_title
    }

    fun descRes(id: AchievementId): Int = when (id) {
        AchievementId.FIRST_STEPS -> R.string.ach_first_tx_desc
        AchievementId.WEEK_MARATHON -> R.string.ach_streak_7_desc
        AchievementId.GETTING_INTO -> R.string.ach_tx_10_desc
        AchievementId.MONTH_DISCIPLINE -> R.string.ach_streak_30_desc
        AchievementId.DIARY -> R.string.ach_tx_50_desc
        AchievementId.DREAM_CAME_TRUE -> R.string.ach_goal_done_desc
        AchievementId.ECONOMIST -> R.string.ach_economist_desc
        AchievementId.WITHIN_LIMITS -> R.string.ach_within_limits_desc
        AchievementId.PIGGY_BANK -> R.string.ach_piggy_desc
        AchievementId.ANALYST -> R.string.ach_analyst_desc
        AchievementId.PAPERLESS -> R.string.ach_paperless_desc
        AchievementId.LEGEND -> R.string.ach_streak_100_desc
    }
}
