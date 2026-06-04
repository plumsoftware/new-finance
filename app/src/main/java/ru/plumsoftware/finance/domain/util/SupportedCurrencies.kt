package ru.plumsoftware.finance.domain.util

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
)

object SupportedCurrencies {
    val popular: List<CurrencyInfo> = listOf(
        CurrencyInfo("RUB", "Российский рубль", "₽"),
        CurrencyInfo("USD", "Доллар США", "$"),
        CurrencyInfo("EUR", "Евро", "€"),
        CurrencyInfo("GBP", "Фунт стерлингов", "£"),
        CurrencyInfo("CNY", "Китайский юань", "¥"),
        CurrencyInfo("KZT", "Казахстанский тенге", "₸"),
        CurrencyInfo("BYN", "Белорусский рубль", "Br"),
    )

    val all: List<CurrencyInfo> = popular + listOf(
        CurrencyInfo("AED", "Дирхам ОАЭ", "د.إ"),
        CurrencyInfo("CHF", "Швейцарский франк", "CHF"),
        CurrencyInfo("JPY", "Японская иена", "¥"),
        CurrencyInfo("TRY", "Турецкая лира", "₺"),
        CurrencyInfo("UAH", "Украинская гривна", "₴"),
        CurrencyInfo("UZS", "Узбекский сум", "so'm"),
    )

    fun find(code: String): CurrencyInfo? =
        all.find { it.code.equals(code, ignoreCase = true) }
        ?: popular.find { it.code.equals(code, ignoreCase = true) }
}
