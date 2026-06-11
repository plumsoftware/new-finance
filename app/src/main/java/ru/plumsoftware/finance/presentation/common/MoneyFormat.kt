package ru.plumsoftware.finance.presentation.common

import android.content.Context
import ru.plumsoftware.finance.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToLong
import kotlin.math.pow

object MoneyFormat {
    private val ruLocale = Locale("ru", "RU")

    fun format(amountMinor: Long, currencyCode: String): String {
        val major = toMajorDouble(amountMinor, currencyCode)
        val formatter = NumberFormat.getCurrencyInstance(ruLocale).apply {
            currency = Currency.getInstance(currencyCode)
            maximumFractionDigits = fractionDigits(currencyCode)
            minimumFractionDigits = fractionDigits(currencyCode)
        }
        return formatter.format(major)
    }

    /** Отображение суммы при вводе (крупные цифры, iOS Calculator). */
    fun formatEntryDisplay(
        context: Context,
        majorAmountDigits: String,
        currencyCode: String,
    ): String {
        if (majorAmountDigits.isBlank()) {
            return context.getString(R.string.money_entry_zero, symbol(currencyCode))
        }
        val major = majorAmountDigits.toDoubleOrNull() ?: 0.0
        val symbols = DecimalFormatSymbols(ruLocale).apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        }
        val pattern = if (fractionDigits(currencyCode) > 0) "#,##0.##" else "#,##0"
        val formatted = DecimalFormat(pattern, symbols).format(major)
        return "$formatted ${symbol(currencyCode)}"
    }

    fun formatWithSignPrefix(
        context: Context,
        amountMinor: Long,
        currencyCode: String,
        isPositive: Boolean,
    ): String {
        val formatted = format(amountMinor, currencyCode)
        return context.getString(
            if (isPositive) R.string.amount_signed_positive else R.string.amount_signed_negative,
            formatted,
        )
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
