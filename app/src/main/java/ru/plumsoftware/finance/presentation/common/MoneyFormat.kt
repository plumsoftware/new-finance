package ru.plumsoftware.finance.presentation.common

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToLong
import kotlin.math.pow

object MoneyFormat {
    private val ruLocale = Locale("ru", "RU")

    fun format(amountMinor: Long, currencyCode: String, showSign: Boolean = false): String {
        val major = toMajorDouble(amountMinor, currencyCode)
        val formatter = NumberFormat.getCurrencyInstance(ruLocale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = fractionDigits(currencyCode)
            minimumFractionDigits = fractionDigits(currencyCode)
        }
        val formatted = formatter.format(major)
        return when {
            !showSign || amountMinor == 0L -> formatted
            amountMinor > 0 -> "+$formatted"
            else -> formatted
        }
    }

    /** Отображение суммы при вводе (крупные цифры, iOS Calculator). */
    fun formatEntryDisplay(majorAmountDigits: String, currencyCode: String): String {
        if (majorAmountDigits.isBlank()) return "0 ${symbol(currencyCode)}"
        val major = majorAmountDigits.toDoubleOrNull() ?: 0.0
        val symbols = DecimalFormatSymbols(ruLocale).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        val pattern = if (fractionDigits(currencyCode) > 0) "#,##0.##" else "#,##0"
        val formatted = DecimalFormat(pattern, symbols).format(major)
        return "$formatted ${symbol(currencyCode)}"
    }

    fun majorDigitsToMinor(majorDigits: String, currencyCode: String): Long {
        val major = majorDigits.replace(',', '.').toDoubleOrNull() ?: 0.0
        val exp = fractionDigits(currencyCode)
        return (major * 10.0.pow(exp.toDouble())).roundToLong()
    }

    fun minorToMajorDigits(amountMinor: Long, currencyCode: String): String {
        val exp = fractionDigits(currencyCode)
        val major = amountMinor / 10.0.pow(exp.toDouble())
        return if (exp == 0) major.toLong().toString() else {
            // для экранов с целыми рублями
            major.toLong().toString()
        }
    }

    private fun toMajorDouble(amountMinor: Long, currencyCode: String): Double {
        val exp = fractionDigits(currencyCode)
        return amountMinor / 10.0.pow(exp.toDouble())
    }

    private fun fractionDigits(currencyCode: String): Int =
        Currency.getInstance(currencyCode).defaultFractionDigits.coerceAtLeast(0)

    fun symbol(currencyCode: String): String =
        Currency.getInstance(currencyCode).getSymbol(ruLocale)
}
