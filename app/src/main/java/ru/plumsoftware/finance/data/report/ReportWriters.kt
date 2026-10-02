package ru.plumsoftware.finance.data.report

import android.content.Context
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.Money
import java.io.File
import java.io.OutputStreamWriter
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

/** Колонки листа «Операции» и CSV (§9). */
private fun operationHeaders(context: Context) = listOf(
    R.string.report_col_date,
    R.string.report_col_time,
    R.string.report_col_title,
    R.string.report_col_category,
    R.string.report_col_type,
    R.string.report_col_account,
    R.string.report_col_amount,
    R.string.report_col_note,
).map(context::getString)

private fun minorToMajor(minor: Long): Double = minor / 100.0

/** CSV: UTF-8 с BOM, разделитель `;`. */
class CsvReportWriter(private val context: Context) {
    fun write(report: Report, file: File) {
        val date = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val time = DateTimeFormatter.ofPattern("HH:mm")
        file.outputStream().use { out ->
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(out, Charsets.UTF_8).use { w ->
                w.write(operationHeaders(context).joinToString(";") { escape(it) })
                w.write("\r\n")
                report.operations.forEach { op ->
                    val signed = if (op.isIncome) op.amountMinor else -op.amountMinor
                    val row = listOf(
                        op.dateTime.format(date),
                        op.dateTime.format(time),
                        op.title,
                        op.category,
                        context.getString(if (op.isIncome) R.string.report_type_income else R.string.report_type_expense),
                        op.account,
                        String.format(java.util.Locale.US, "%.2f", minorToMajor(signed)).replace('.', ','),
                        op.note.orEmpty(),
                    )
                    w.write(row.joinToString(";") { escape(it) })
                    w.write("\r\n")
                }
            }
        }
    }

    private fun escape(v: String): String =
        if (v.any { it == ';' || it == '"' || it == '\n' || it == '\r' }) "\"" + v.replace("\"", "\"\"") + "\"" else v
}

/** Excel: листы «Итоги», «Категории», «Операции»; первая строка закреплена, автофильтр. */
class XlsxReportWriter(private val context: Context) {
    fun write(report: Report, file: File) {
        XSSFWorkbook().use { wb ->
            val helper = wb.creationHelper
            val bold = wb.createFont().apply { this.bold = true }
            val header = wb.createCellStyle().apply {
                setFont(bold)
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
            }
            val money = wb.createCellStyle().apply {
                dataFormat = helper.createDataFormat().getFormat("#,##0.00\\ \"₽\"")
            }
            val percent = wb.createCellStyle().apply {
                dataFormat = helper.createDataFormat().getFormat("0.0%")
            }
            val dateStyle = wb.createCellStyle().apply {
                dataFormat = helper.createDataFormat().getFormat("dd.mm.yyyy")
            }

            // Итоги
            wb.createSheet(context.getString(R.string.report_sheet_summary)).apply {
                val rows = listOf(
                    context.getString(R.string.report_period) to report.periodLabel,
                    context.getString(R.string.report_expenses) to report.totalExpenseMinor,
                    context.getString(R.string.report_incomes) to report.totalIncomeMinor,
                    context.getString(R.string.report_diff) to report.diffMinor,
                    context.getString(R.string.report_compare) to (report.comparePercent?.let { "${if (it > 0) "+" else ""}$it%" } ?: "—"),
                )
                createRow(0).apply {
                    createCell(0).apply { setCellValue(report.title); cellStyle = header }
                    createCell(1).apply { cellStyle = header }
                }
                rows.forEachIndexed { i, (k, v) ->
                    createRow(i + 1).apply {
                        createCell(0).setCellValue(k)
                        val cell = createCell(1)
                        when (v) {
                            is Long -> { cell.setCellValue(minorToMajor(v)); cell.cellStyle = money }
                            else -> cell.setCellValue(v.toString())
                        }
                    }
                }
                if (report.excludedNames.isNotEmpty()) {
                    createRow(rows.size + 2).createCell(0)
                        .setCellValue(context.getString(R.string.report_excluded, report.excludedNames.joinToString(", ")))
                }
                createFreezePane(0, 1)
                setColumnWidth(0, 28 * 256)
                setColumnWidth(1, 22 * 256)
            }

            // Категории
            wb.createSheet(context.getString(R.string.report_sheet_categories)).apply {
                createRow(0).apply {
                    listOf(R.string.report_col_category, R.string.report_col_amount, R.string.report_col_share).forEachIndexed { i, r ->
                        createCell(i).apply { setCellValue(context.getString(r)); cellStyle = header }
                    }
                }
                report.categories.forEachIndexed { i, c ->
                    createRow(i + 1).apply {
                        createCell(0).setCellValue(c.name)
                        createCell(1).apply { setCellValue(minorToMajor(c.amountMinor)); cellStyle = money }
                        createCell(2).apply { setCellValue(c.share); cellStyle = percent }
                    }
                }
                createFreezePane(0, 1)
                if (report.categories.isNotEmpty()) setAutoFilter(CellRangeAddress(0, report.categories.size, 0, 2))
                setColumnWidth(0, 26 * 256)
                setColumnWidth(1, 18 * 256)
                setColumnWidth(2, 10 * 256)
            }

            // Операции
            wb.createSheet(context.getString(R.string.report_sheet_operations)).apply {
                val headers = operationHeaders(context)
                createRow(0).apply { headers.forEachIndexed { i, h -> createCell(i).apply { setCellValue(h); cellStyle = header } } }
                val time = DateTimeFormatter.ofPattern("HH:mm")
                val zone = ZoneId.systemDefault()
                report.operations.forEachIndexed { i, op ->
                    createRow(i + 1).apply {
                        createCell(0).apply {
                            setCellValue(Date.from(op.dateTime.toLocalDate().atStartOfDay(zone).toInstant()))
                            cellStyle = dateStyle
                        }
                        createCell(1).setCellValue(op.dateTime.format(time))
                        createCell(2).setCellValue(op.title)
                        createCell(3).setCellValue(op.category)
                        createCell(4).setCellValue(
                            context.getString(if (op.isIncome) R.string.report_type_income else R.string.report_type_expense),
                        )
                        createCell(5).setCellValue(op.account)
                        createCell(6).apply {
                            setCellValue(minorToMajor(if (op.isIncome) op.amountMinor else -op.amountMinor))
                            cellStyle = money
                        }
                        createCell(7).setCellValue(op.note.orEmpty())
                    }
                }
                createFreezePane(0, 1)
                setAutoFilter(CellRangeAddress(0, report.operations.size.coerceAtLeast(1), 0, headers.lastIndex))
                listOf(12, 8, 28, 20, 10, 16, 16, 30).forEachIndexed { i, w -> setColumnWidth(i, w * 256) }
            }

            file.outputStream().use { wb.write(it) }
        }
    }
}

/** Подпись суммы для PDF. */
internal fun pdfMoney(minor: Long, currency: String) = Money.format(minor, currency)
