package ru.plumsoftware.finance.domain.model

import android.content.Context
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import ru.plumsoftware.finance.R

data class ExportStyles(
    val header: XSSFCellStyle,
    val income: XSSFCellStyle,
    val expense: XSSFCellStyle,
    val date: XSSFCellStyle,
    val title: XSSFCellStyle,
)

class XlsxExporter(private val context: Context) { // Добавлен Context

    // Кэш для стилей валют во избежание превышения лимитов стилей Excel
    private val cellStyleCache = mutableMapOf<String, XSSFCellStyle>()

    fun export(data: ExportData, outputFile: File) {
        val workbook = XSSFWorkbook()
        cellStyleCache.clear() // очищаем кэш перед генерацией

        val styles = buildStyles(workbook)

        createSummarySheet(workbook, data, styles)
        createTransactionsSheet(workbook, data, styles)
        createAccountsSheet(workbook, data, styles)

        FileOutputStream(outputFile).use { fos -> workbook.write(fos) }
        workbook.close()
    }

    private fun buildStyles(wb: XSSFWorkbook): ExportStyles {
        val headerFont = wb.createFont().apply {
            bold = true
            fontHeightInPoints = 11
            color = IndexedColors.WHITE.index
        }
        val headerStyle = wb.createCellStyle().apply {
            setFont(headerFont)
            fillForegroundColor = IndexedColors.BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
        }

        // Добавлены базовые стили для совместимости с конструктором ExportStyles
        val incomeStyle = wb.createCellStyle().apply {
            setFont(wb.createFont().apply { color = IndexedColors.GREEN.index })
            dataFormat = wb.createDataFormat().getFormat("#,##0.00")
        }

        val expenseStyle = wb.createCellStyle().apply {
            setFont(wb.createFont().apply { color = IndexedColors.RED.index })
            dataFormat = wb.createDataFormat().getFormat("#,##0.00")
        }

        val dateStyle = wb.createCellStyle().apply {
            dataFormat = wb.createDataFormat().getFormat("dd.mm.yyyy hh:mm")
        }

        val titleStyle = wb.createCellStyle().apply {
            setFont(wb.createFont().apply { bold = true; fontHeightInPoints = 16 })
        }

        // Возвращаем все 5 параметров в строгом порядке, соответствующем конструктору
        return ExportStyles(
            header = headerStyle,
            income = incomeStyle,
            expense = expenseStyle,
            date = dateStyle,
            title = titleStyle
        )
    }

    // Динамический генератор стилей для денежных значений на основе кода валюты
    private fun getMoneyStyle(
        wb: XSSFWorkbook,
        isIncome: Boolean,
        currencyCode: String
    ): XSSFCellStyle {
        val color = if (isIncome) IndexedColors.GREEN.index else IndexedColors.RED.index
        val key = "${color}_$currencyCode"
        return cellStyleCache.getOrPut(key) {
            wb.createCellStyle().apply {
                setFont(wb.createFont().apply { this.color = color })
                val symbol = when (currencyCode) {
                    "RUB" -> "₽"
                    "USD" -> "$"
                    "EUR" -> "€"
                    else -> currencyCode
                }
                dataFormat = wb.createDataFormat().getFormat("#,##0.00 \"$symbol\"")
            }
        }
    }

    private fun createSummarySheet(wb: XSSFWorkbook, data: ExportData, styles: ExportStyles) {
        val sheetNameSummary = context.getString(R.string.excel_sheet_summary)
        val sheetTransactions = context.getString(R.string.excel_sheet_transactions)

        val sheet = wb.createSheet(sheetNameSummary)
        var rowIdx = 0

        sheet.createRow(rowIdx++).createCell(0).apply {
            setCellValue(context.getString(R.string.pdf_report_title))
            cellStyle = styles.title
        }
        sheet.createRow(rowIdx++).createCell(0).setCellValue(
            context.getString(R.string.pdf_report_period, data.periodLabel)
        )
        sheet.createRow(rowIdx++).createCell(0).setCellValue(
            context.getString(R.string.pdf_report_generated, formatDate(data.generatedAt))
        )
        rowIdx++ // пустая строка

        // Строка доходов
        val incomeRow = sheet.createRow(rowIdx++)
        incomeRow.createCell(0).setCellValue(context.getString(R.string.excel_income_label))
        incomeRow.createCell(1).apply {
            val typeIncome = context.getString(R.string.pdf_type_income)
            cellFormula = "SUMIF('$sheetTransactions'!B:B,\"$typeIncome\",'$sheetTransactions'!C:C)"
            cellStyle = getMoneyStyle(wb, true, data.currencyCode)
        }

        // Строка расходов
        val expenseRow = sheet.createRow(rowIdx++)
        expenseRow.createCell(0).setCellValue(context.getString(R.string.excel_expense_label))
        expenseRow.createCell(1).apply {
            val typeExpense = context.getString(R.string.pdf_type_expense)
            cellFormula = "SUMIF('$sheetTransactions'!B:B,\"$typeExpense\",'$sheetTransactions'!C:C)"
            cellStyle = getMoneyStyle(wb, false, data.currencyCode)
        }

        // Строка баланса
        val balanceRow = sheet.createRow(rowIdx++)
        balanceRow.createCell(0).setCellValue(context.getString(R.string.excel_balance_label))
        balanceRow.createCell(1).apply {
            cellFormula = "B${incomeRow.rowNum + 1}-B${expenseRow.rowNum + 1}"
            cellStyle = getMoneyStyle(wb, true, data.currencyCode)
        }

        sheet.setColumnWidth(0, 25 * 256)
        sheet.setColumnWidth(1, 18 * 256)
    }

    private fun createTransactionsSheet(wb: XSSFWorkbook, data: ExportData, styles: ExportStyles) {
        val sheetNameTx = context.getString(R.string.excel_sheet_transactions)
        val sheet = wb.createSheet(sheetNameTx)

        val headers = listOf(
            context.getString(R.string.pdf_col_date),
            context.getString(R.string.pdf_col_type),
            context.getString(R.string.pdf_col_amount),
            context.getString(R.string.excel_col_currency),
            context.getString(R.string.pdf_col_category),
            context.getString(R.string.pdf_col_account),
            context.getString(R.string.pdf_col_note)
        )

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).apply {
                setCellValue(title)
                cellStyle = styles.header
            }
        }

        data.transactions
            .sortedByDescending { it.date }
            .forEachIndexed { index, tx ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).apply {
                    setCellValue(Date(tx.date))
                    cellStyle = styles.date
                }

                val isIncome = tx.type == "Income" || tx.type == "Доход"
                val typeLabel = if (isIncome) {
                    context.getString(R.string.pdf_type_income)
                } else {
                    context.getString(R.string.pdf_type_expense)
                }

                row.createCell(1).setCellValue(typeLabel)
                row.createCell(2).apply {
                    setCellValue(tx.amountMinor / 100.0)
                    cellStyle = getMoneyStyle(wb, isIncome, tx.currencyCode)
                }
                row.createCell(3).setCellValue(tx.currencyCode)
                row.createCell(4).setCellValue(tx.categoryName ?: "")
                row.createCell(5).setCellValue(tx.accountName)
                row.createCell(6).setCellValue(tx.note ?: "")
            }

        val columnWidths = listOf(20 * 256, 12 * 256, 15 * 256, 10 * 256, 22 * 256, 22 * 256, 30 * 256)
        columnWidths.forEachIndexed { i, width -> sheet.setColumnWidth(i, width) }
        sheet.createFreezePane(0, 1)
    }

    private fun createAccountsSheet(wb: XSSFWorkbook, data: ExportData, styles: ExportStyles) {
        val sheetNameAccounts = context.getString(R.string.excel_sheet_accounts)
        val sheet = wb.createSheet(sheetNameAccounts)

        val headerRow = sheet.createRow(0)
        val headers = listOf(
            context.getString(R.string.excel_col_name),
            context.getString(R.string.excel_col_type),
            context.getString(R.string.excel_col_currency),
            context.getString(R.string.excel_col_balance)
        )

        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).apply {
                setCellValue(title)
                cellStyle = styles.header
            }
        }

        data.accounts.forEachIndexed { index, acc ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(acc.name)
            row.createCell(1).setCellValue(acc.type)
            row.createCell(2).setCellValue(acc.currencyCode)
            row.createCell(3).apply {
                setCellValue(acc.balanceMinor / 100.0)
                cellStyle = getMoneyStyle(wb, acc.balanceMinor >= 0, acc.currencyCode)
            }
        }

        val columnWidths = listOf(25 * 256, 15 * 256, 10 * 256, 18 * 256)
        columnWidths.forEachIndexed { i, width -> sheet.setColumnWidth(i, width) }
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(millis))
}