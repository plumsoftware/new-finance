package ru.plumsoftware.finance.domain.achievements

import java.time.LocalDate
import java.time.YearMonth

/** 12 достижений (ТЗ §6.12). */
enum class AchievementId(val key: String, val target: Int, val isDays: Boolean = false) {
    FIRST_STEPS("first_tx", 1),
    WEEK_MARATHON("streak_7", 7, isDays = true),
    GETTING_INTO("tx_10", 10),
    MONTH_DISCIPLINE("streak_30", 30, isDays = true),
    DIARY("tx_50", 50),
    DREAM_CAME_TRUE("goal_done", 1),
    ECONOMIST("economist_1000", 1_000),
    WITHIN_LIMITS("within_limits_7", 7, isDays = true),
    PIGGY_BANK("piggy_100k", 100_000),
    ANALYST("analyst", 1),
    PAPERLESS("paperless_5", 5),
    LEGEND("streak_100", 100, isDays = true),
    ;

    companion object {
        val keys: Set<String> = entries.map { it.key }.toSet()
        fun byKey(key: String): AchievementId? = entries.firstOrNull { it.key == key }
    }
}

data class AchievementProgress(val id: AchievementId, val value: Int) {
    val isReached: Boolean get() = value >= id.target
    val fraction: Float get() = (value.toFloat() / id.target).coerceIn(0f, 1f)
}

data class AchievementInputs(
    val transactionsCount: Int,
    val streakDays: Int,
    val completedGoals: Int,
    /** Умная экономия, ₽ (целые). */
    val smartSavedRub: Long,
    val daysWithinLimits: Int,
    /** Накоплено в целях, ₽ (целые). */
    val goalsSavedRub: Long,
    val reportsExported: Int,
    val receiptsScanned: Int,
)

object AchievementsEngine {

    fun progress(i: AchievementInputs): List<AchievementProgress> = AchievementId.entries.map { id ->
        val v = when (id) {
            AchievementId.FIRST_STEPS, AchievementId.GETTING_INTO, AchievementId.DIARY -> i.transactionsCount
            AchievementId.WEEK_MARATHON, AchievementId.MONTH_DISCIPLINE, AchievementId.LEGEND -> i.streakDays
            AchievementId.DREAM_CAME_TRUE -> i.completedGoals
            AchievementId.ECONOMIST -> i.smartSavedRub.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            AchievementId.WITHIN_LIMITS -> i.daysWithinLimits
            AchievementId.PIGGY_BANK -> i.goalsSavedRub.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            AchievementId.ANALYST -> i.reportsExported
            AchievementId.PAPERLESS -> i.receiptsScanned
        }
        AchievementProgress(id, v)
    }

    data class Expense(val date: LocalDate, val categoryId: Long?, val amount: Long)

    /**
     * Дней подряд (заканчивая вчера), в которые ни одна категория с лимитом не превысила
     * месячный лимит нарастающим итогом. Без лимитов — 0.
     */
    fun daysWithinLimits(expenses: List<Expense>, limits: Map<Long, Long>, today: LocalDate, maxDays: Int = 7): Int {
        if (limits.isEmpty()) return 0
        val byMonthCat = expenses.filter { it.categoryId != null && it.categoryId in limits }
            .groupBy { YearMonth.from(it.date) }
        var count = 0
        var d = today.minusDays(1)
        while (count < maxDays) {
            val monthExpenses = byMonthCat[YearMonth.from(d)].orEmpty().filter { !it.date.isAfter(d) }
            val spentByCat = monthExpenses.groupBy { it.categoryId!! }.mapValues { e -> e.value.sumOf { it.amount } }
            val exceeded = spentByCat.any { (cat, spent) -> spent > (limits[cat] ?: Long.MAX_VALUE) }
            if (exceeded) break
            count++
            d = d.minusDays(1)
        }
        return count
    }
}
