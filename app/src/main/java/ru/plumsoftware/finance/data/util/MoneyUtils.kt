package ru.plumsoftware.finance.data.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

object MoneyUtils {
    fun toMinorUnits(amountMajor: BigDecimal, currencyCode: String): Long {
        val fractionDigits = Currency.getInstance(currencyCode).defaultFractionDigits
        val multiplier = BigDecimal.TEN.pow(fractionDigits)
        return amountMajor.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }

    fun toMajorUnits(amountMinor: Long, currencyCode: String): BigDecimal {
        val fractionDigits = Currency.getInstance(currencyCode).defaultFractionDigits
        val divisor = BigDecimal.TEN.pow(fractionDigits)
        return BigDecimal.valueOf(amountMinor).divide(divisor)
    }
}
