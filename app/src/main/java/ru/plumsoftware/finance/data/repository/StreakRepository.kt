package ru.plumsoftware.finance.data.repository

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.StreakDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.entity.StreakDataEntity
import ru.plumsoftware.finance.domain.model.StreakData

class StreakRepository(
    private val transactionDao: TransactionDao,
    private val streakDao: StreakDao,
) {
    fun observe(): Flow<StreakData> = streakDao.observe().map { it?.toDomain() ?: StreakData() }

    suspend fun calculateAndSave(): StreakData {
        val zone = ZoneId.systemDefault()
        val dates = transactionDao.getAllDates()
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .distinct()
            .sortedDescending()

        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)

        val todayHasActivity = dates.firstOrNull() == today
        val lastActivityDate = dates.firstOrNull()
            ?.atStartOfDay(zone)
            ?.toInstant()
            ?.toEpochMilli()
            ?: 0L

        val currentStart = when {
            dates.contains(today) -> today
            dates.contains(yesterday) -> yesterday
            else -> null
        }

        var current = 0
        if (currentStart != null) {
            var cursor = currentStart
            while (dates.contains(cursor)) {
                current++
                cursor = cursor?.minusDays(1)
            }
        }

        var longest = 0
        var run = 0
        var prev: LocalDate? = null
        dates.sorted().forEach { date ->
            run = if (prev == null || ChronoUnit.DAYS.between(prev, date) == 1L) run + 1 else 1
            if (run > longest) longest = run
            prev = date
        }

        val existing = streakDao.get()
        val data = StreakData(
            currentStreak = current,
            longestStreak = maxOf(longest, existing?.longestStreak ?: 0),
            lastActivityDate = lastActivityDate,
            todayHasActivity = todayHasActivity,
            totalUnlocked = existing?.totalUnlocked ?: 0,
        )
        streakDao.save(data.toEntity())
        return data
    }

    suspend fun setTotalUnlocked(total: Int) {
        val current = streakDao.get()?.toDomain() ?: StreakData()
        if (current.totalUnlocked != total) {
            streakDao.save(current.copy(totalUnlocked = total).toEntity())
        }
    }
}

private fun StreakDataEntity.toDomain(): StreakData = StreakData(
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastActivityDate = lastActivityDate,
    todayHasActivity = todayHasActivity,
    totalUnlocked = totalUnlocked,
)

private fun StreakData.toEntity(): StreakDataEntity = StreakDataEntity(
    id = 1,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastActivityDate = lastActivityDate,
    todayHasActivity = todayHasActivity,
    totalUnlocked = totalUnlocked,
)
