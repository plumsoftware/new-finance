package ru.plumsoftware.finance.domain.util

import androidx.annotation.StringRes
import ru.plumsoftware.finance.R

data class CurrencyInfo(
    val code: String,
    @StringRes val nameRes: Int,
    val symbol: String,
)

object SupportedCurrencies {
    val popular: List<CurrencyInfo> = listOf(
        CurrencyInfo("RUB", R.string.currency_name_rub, "₽"),
        CurrencyInfo("USD", R.string.currency_name_usd, "$"),
        CurrencyInfo("EUR", R.string.currency_name_eur, "€"),
        CurrencyInfo("GBP", R.string.currency_name_gbp, "£"),
        CurrencyInfo("CNY", R.string.currency_name_cny, "¥"),
        CurrencyInfo("KZT", R.string.currency_name_kzt, "₸"),
        CurrencyInfo("BYN", R.string.currency_name_byn, "Br"),
    )

    val all: List<CurrencyInfo> = popular + listOf(
        CurrencyInfo("AED", R.string.currency_name_aed, "د.إ"),
        CurrencyInfo("CHF", R.string.currency_name_chf, "CHF"),
        CurrencyInfo("JPY", R.string.currency_name_jpy, "¥"),
        CurrencyInfo("TRY", R.string.currency_name_try, "₺"),
        CurrencyInfo("UAH", R.string.currency_name_uah, "₴"),
        CurrencyInfo("UZS", R.string.currency_name_uzs, "so'm"),
    )

    fun find(code: String): CurrencyInfo? =
        all.find { it.code.equals(code, ignoreCase = true) }
        ?: popular.find { it.code.equals(code, ignoreCase = true) }
}
