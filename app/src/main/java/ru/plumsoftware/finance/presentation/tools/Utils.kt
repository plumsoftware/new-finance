package ru.plumsoftware.finance.presentation.tools

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// Безопасный парсинг отформатированных строк в Double
fun String.cleanDouble(): Double? {
    return this.replace(" ", "").replace(",", ".").toDoubleOrNull()
}

// Безопасный парсинг отформатированных строк в Int
fun String.cleanInt(): Int? {
    return this.replace(" ", "").toIntOrNull()
}

// Форматирование числовой строки для визуальной трансформации
fun formatInputDigits(raw: String, isIntegerOnly: Boolean = false): String {
    if (raw.isEmpty()) return ""
    val parts = raw.split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else null

    val groupedInt = intPart.reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()

    return if (!isIntegerOnly && raw.contains(".")) {
        if (decPart != null) "$groupedInt.${decPart.take(2)}" else "$groupedInt."
    } else {
        groupedInt
    }
}

// Форматирование вывода результатов с разделением разрядов пробелом и запятой
fun formatOutput(value: Double, decimals: Int = 2): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }
    val pattern = if (decimals > 0) {
        "#,##0." + "0".repeat(decimals)
    } else {
        "#,##0"
    }
    val df = DecimalFormat(pattern, symbols)
    return df.format(value)
}