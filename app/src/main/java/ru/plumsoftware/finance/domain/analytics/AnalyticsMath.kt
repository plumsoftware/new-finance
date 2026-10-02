package ru.plumsoftware.finance.domain.analytics

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Режим периода Аналитики (§6.4 п.2). */
enum class PeriodMode { WEEK, MONTH, YEAR, CUSTOM }

data class DateRange(val start: LocalDate, val end: LocalDate) {
    val lengthDays: Int get() = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
    operator fun contains(d: LocalDate): Boolean = !d.isBefore(start) && !d.isAfter(end)
}

enum class BucketUnit { DAY, MONTH }

data class Bucket(val start: LocalDate, val end: LocalDate, val total: Long)

object AnalyticsMath {

    /** §8.3: диапазон по режиму. */
    fun range(mode: PeriodMode, today: LocalDate, custom: DateRange? = null): DateRange = when (mode) {
        PeriodMode.WEEK -> {
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            DateRange(monday, monday.plusDays(6))
        }
        PeriodMode.MONTH -> {
            val ym = YearMonth.from(today)
            DateRange(ym.atDay(1), ym.atEndOfMonth())
        }
        PeriodMode.YEAR -> DateRange(LocalDate.of(today.year, 1, 1), LocalDate.of(today.year, 12, 31))
        PeriodMode.CUSTOM -> custom ?: DateRange(today.minusDays(29), today)
    }

    /** Бакеты: день, если длина ≤ 62 дней и не «Год», иначе месяц. */
    fun bucketUnit(mode: PeriodMode, range: DateRange): BucketUnit =
        if (mode != PeriodMode.YEAR && range.lengthDays <= 62) BucketUnit.DAY else BucketUnit.MONTH

    fun buckets(range: DateRange, unit: BucketUnit, expensesByDate: Map<LocalDate, Long>): List<Bucket> {
        val result = mutableListOf<Bucket>()
        when (unit) {
            BucketUnit.DAY -> {
                var d = range.start
                while (!d.isAfter(range.end)) {
                    result += Bucket(d, d, expensesByDate[d] ?: 0L)
                    d = d.plusDays(1)
                }
            }
            BucketUnit.MONTH -> {
                var ym = YearMonth.from(range.start)
                val last = YearMonth.from(range.end)
                while (!ym.isAfter(last)) {
                    val s = maxOf(ym.atDay(1), range.start)
                    val e = minOf(ym.atEndOfMonth(), range.end)
                    val total = expensesByDate.filterKeys { it in DateRange(s, e) }.values.sum()
                    result += Bucket(s, e, total)
                    ym = ym.plusMonths(1)
                }
            }
        }
        return result
    }

    /** `L` — прошедших дней в диапазоне (включая сегодня). */
    fun elapsedDays(range: DateRange, today: LocalDate): Int = when {
        today.isBefore(range.start) -> 0
        today.isAfter(range.end) -> range.lengthDays
        else -> (ChronoUnit.DAYS.between(range.start, today) + 1).toInt()
    }

    /**
     * Предыдущий период для сравнения (§8.3): для месяца — те же числа прошлого месяца,
     * иначе `[start − L, start − 1]`.
     */
    fun previousRange(mode: PeriodMode, range: DateRange, today: LocalDate): DateRange? {
        val l = elapsedDays(range, today)
        if (l <= 0) return null
        return if (mode == PeriodMode.MONTH) {
            val prevStart = range.start.minusMonths(1)
            val prevYm = YearMonth.from(prevStart)
            val endDay = minOf(l, prevYm.lengthOfMonth())
            DateRange(prevStart, prevYm.atDay(endDay))
        } else {
            DateRange(range.start.minusDays(l.toLong()), range.start.minusDays(1))
        }
    }

    /** `p = round((total − prev) / prev × 100)`; `null`, если сравнивать не с чем. */
    fun comparePercent(total: Long, prev: Long): Int? =
        if (prev <= 0L) null else ((total - prev).toDouble() / prev * 100).roundToInt()

    /** Индексы бакетов с подписью оси (§6.4 п.4). */
    fun axisLabelIndices(mode: PeriodMode, unit: BucketUnit, buckets: List<Bucket>): Set<Int> {
        val n = buckets.size
        if (n == 0) return emptySet()
        return when {
            mode == PeriodMode.WEEK || mode == PeriodMode.YEAR -> buckets.indices.toSet()
            mode == PeriodMode.MONTH -> buckets.indices.filter { i ->
                val day = buckets[i].start.dayOfMonth
                day == 1 || day % 5 == 0
            }.toSet()
            unit == BucketUnit.DAY -> {
                val step = ceil(n / 6.0).toInt().coerceAtLeast(1)
                buckets.indices.filter { it % step == 0 }.toSet()
            }
            else -> buckets.indices.toSet()
        }
    }
}
