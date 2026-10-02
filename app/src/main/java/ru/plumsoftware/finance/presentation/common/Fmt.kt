package ru.plumsoftware.finance.presentation.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

/** Неразрывный пробел — разделитель тысяч и пробел перед знаком валюты (ТЗ §11). */
const val NBSP = ' '

/** Типографский минус U+2212. */
const val MINUS = '−'

/**
 * Форматирование денег по ТЗ §11: `1 234 567,89 ₽`.
 * Дробная часть показывается, только если не нулевая (или [forceFraction] — для балансов счетов).
 */
object Money {

    fun symbol(currencyCode: String): String = when (currencyCode) {
        "RUB" -> "₽"
        else -> runCatching { Currency.getInstance(currencyCode).getSymbol(Locale.getDefault()) }
            .getOrDefault(currencyCode)
    }

    private fun fractionDigits(currencyCode: String): Int =
        runCatching { Currency.getInstance(currencyCode).defaultFractionDigits }.getOrDefault(2).coerceAtLeast(0)

    fun toMajor(amountMinor: Long, currencyCode: String = "RUB"): BigDecimal =
        BigDecimal.valueOf(amountMinor).movePointLeft(fractionDigits(currencyCode))

    fun fromMajor(major: Double, currencyCode: String = "RUB"): Long =
        BigDecimal.valueOf(major).movePointRight(fractionDigits(currencyCode))
            .setScale(0, RoundingMode.HALF_UP).toLong()

    /** Число без знака валюты: `1 234 567,89`. */
    fun number(amountMinor: Long, currencyCode: String = "RUB", forceFraction: Boolean = false): String {
        val digits = fractionDigits(currencyCode)
        val major = toMajor(abs(amountMinor), currencyCode)
        val whole = major.setScale(0, RoundingMode.DOWN).toLong()
        val frac = major.subtract(BigDecimal.valueOf(whole))
        val grouped = groupThousands(whole)
        val showFraction = digits > 0 && (forceFraction || frac.signum() != 0)
        val sign = if (amountMinor < 0) MINUS.toString() else ""
        return if (showFraction) {
            val fracStr = frac.movePointRight(digits).setScale(0, RoundingMode.HALF_UP).toLong()
                .toString().padStart(digits, '0')
            "$sign$grouped,$fracStr"
        } else {
            "$sign$grouped"
        }
    }

    /** `1 234 ₽`, отрицательные — с типографским минусом. */
    fun format(amountMinor: Long, currencyCode: String = "RUB", forceFraction: Boolean = false): String =
        "${number(amountMinor, currencyCode, forceFraction)}$NBSP${symbol(currencyCode)}"

    /** Целые рубли без копеек, округление до рубля. */
    fun formatRounded(amountMinor: Long, currencyCode: String = "RUB"): String {
        val digits = fractionDigits(currencyCode)
        val rounded = BigDecimal.valueOf(amountMinor).movePointLeft(digits).setScale(0, RoundingMode.HALF_UP)
            .movePointRight(digits).toLong()
        return format(rounded, currencyCode)
    }

    /** Расход `−990 ₽`, доход `+25 000 ₽`. */
    fun signed(amountMinor: Long, isIncome: Boolean, currencyCode: String = "RUB"): String {
        val body = format(abs(amountMinor), currencyCode)
        return if (isIncome) "+$body" else "$MINUS$body"
    }

    /** Знак по значению: `+530 ₽` / `−250 ₽`. */
    fun withSign(amountMinor: Long, currencyCode: String = "RUB"): String =
        if (amountMinor >= 0) "+${format(amountMinor, currencyCode)}" else format(amountMinor, currencyCode)

    /** Сокращения для графиков: `990`, `1,2к`, `3,5 млн`. */
    fun short(amountMinor: Long, currencyCode: String = "RUB"): String {
        val major = toMajor(abs(amountMinor), currencyCode).toDouble()
        val sign = if (amountMinor < 0) MINUS.toString() else ""
        return sign + when {
            major >= 1_000_000 -> "${oneDecimal(major / 1_000_000)}${NBSP}млн"
            major >= 1_000 -> "${oneDecimal(major / 1_000)}к"
            else -> major.toLong().toString()
        }
    }

    private fun oneDecimal(v: Double): String {
        val bd = BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros()
        return bd.toPlainString().replace('.', ',')
    }

    fun groupThousands(value: Long): String {
        val s = abs(value).toString()
        val sb = StringBuilder()
        s.forEachIndexed { i, c ->
            if (i > 0 && (s.length - i) % 3 == 0) sb.append(NBSP)
            sb.append(c)
        }
        return sb.toString()
    }
}

/**
 * Склонение по правилам русского языка (ТЗ §11): `plural(n, "цель", "цели", "целей")`.
 */
fun plural(n: Long, one: String, few: String, many: String): String {
    val n100 = abs(n) % 100
    val n10 = abs(n) % 10
    return when {
        n100 in 11..14 -> many
        n10 == 1L -> one
        n10 in 2..4 -> few
        else -> many
    }
}

fun plural(n: Int, one: String, few: String, many: String): String = plural(n.toLong(), one, few, many)

/** `5 целей` */
fun pluralCount(n: Int, one: String, few: String, many: String): String = "$n ${plural(n, one, few, many)}"

/** Даты по ТЗ §11. */
object DateFmt {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val locale: Locale get() = Locale.getDefault()

    fun toLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun startOfDay(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    /** Конец дня (исключительно): начало следующего дня. */
    fun endOfDayExclusive(date: LocalDate): Long = startOfDay(date.plusDays(1))

    /** «27 сентября» */
    fun dayMonth(date: LocalDate): String = DateTimeFormatter.ofPattern("d MMMM", locale).format(date)

    /** «27 сент» */
    fun dayMonthShort(date: LocalDate): String = "${date.dayOfMonth} ${monthShort(date)}"

    /** «сент» */
    fun monthShort(date: LocalDate): String =
        DateTimeFormatter.ofPattern("MMM", locale).format(date).trimEnd('.')

    /** «сентябрь» */
    fun monthStandalone(date: LocalDate): String =
        date.month.getDisplayName(TextStyle.FULL_STANDALONE, locale).lowercase(locale)

    /** «янв» (именительный, для осей графиков) */
    fun monthStandaloneShort(date: LocalDate): String =
        date.month.getDisplayName(TextStyle.SHORT_STANDALONE, locale).lowercase(locale).trimEnd('.')

    /** «сентябрь 2026» */
    fun monthYear(date: LocalDate): String = "${monthStandalone(date)} ${date.year}"

    /** «Сентябрь 2026» */
    fun monthYearCapitalized(date: LocalDate): String = monthYear(date).replaceFirstChar { it.titlecase(locale) }

    /** «1–27 сентября», «3 авг – 27 сент», «28 дек 2025 – 3 янв 2026» */
    fun range(start: LocalDate, end: LocalDate): String = when {
        start == end -> dayMonth(start)
        start.year == end.year && start.month == end.month ->
            "${start.dayOfMonth}–${dayMonth(end)}"
        start.year == end.year -> "${dayMonthShort(start)} – ${dayMonthShort(end)}"
        else -> "${dayMonthShort(start)} ${start.year} – ${dayMonthShort(end)} ${end.year}"
    }

    /** «Пн» */
    fun weekdayShort(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
            .replaceFirstChar { it.titlecase(locale) }

    /** «13:15» */
    fun time(millis: Long): String =
        DateTimeFormatter.ofPattern("HH:mm", locale).format(Instant.ofEpochMilli(millis).atZone(zone))

    /** «27.09.2026» */
    fun numeric(date: LocalDate): String = DateTimeFormatter.ofPattern("dd.MM.yyyy", locale).format(date)
}

/** Поиск операции по сумме: «990», «1 200», «990,5». */
object AmountInputMatcher {
    fun matches(amountMinor: Long, query: String): Boolean {
        val normalized = query.replace(" ", "").replace(NBSP.toString(), "").replace('.', ',')
        if (normalized.isEmpty() || normalized.any { !it.isDigit() && it != ',' }) return false
        val whole = amountMinor / 100
        val frac = amountMinor % 100
        val full = if (frac == 0L) whole.toString() else "$whole,${frac.toString().padStart(2, '0')}"
        return full.startsWith(normalized)
    }
}
