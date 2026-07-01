package ru.plumsoftware.finance.domain.model

import android.content.Context
import ru.plumsoftware.finance.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExporter(private val context: Context) { // Добавлен Context

    fun export(data: ExportData): String {
        val sb = StringBuilder()

        // Локализованные заголовки колонок
        val headers = listOf(
            context.getString(R.string.pdf_col_date),
            context.getString(R.string.pdf_col_type),
            context.getString(R.string.pdf_col_amount),
            context.getString(R.string.excel_col_currency),
            context.getString(R.string.pdf_col_category),
            context.getString(R.string.pdf_col_account),
            context.getString(R.string.pdf_col_note)
        )
        sb.append(headers.joinToString(";")).append("\n")

        data.transactions
            .sortedByDescending { it.date }
            .forEach { tx ->
                val isIncome = tx.type == "Income" || tx.type == "Доход"
                val typeLabel = if (isIncome) {
                    context.getString(R.string.pdf_type_income)
                } else {
                    context.getString(R.string.pdf_type_expense)
                }

                // Денежные суммы форматируются без знака доллара, используя локаль
                val amountFormatted = formatMoney(tx.amountMinor)

                val row = listOf(
                    formatDate(tx.date),
                    typeLabel,
                    amountFormatted,
                    tx.currencyCode,
                    tx.categoryName ?: "",
                    tx.accountName,
                    tx.note ?: ""
                )
                sb.append(row.joinToString(";")).append("\n")
            }

        return sb.toString()
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))

    private fun formatMoney(minor: Long): String =
        String.format(Locale.getDefault(), "%.2f", minor / 100.0)
}