package ru.plumsoftware.finance.domain.model

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDFont
import com.tom_roush.pdfbox.pdmodel.font.PDType0Font
import ru.plumsoftware.finance.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {

    companion object {
        private val PAGE_WIDTH = PDRectangle.A4.width
        private val PAGE_HEIGHT = PDRectangle.A4.height
        private const val MARGIN = 40f
        private const val ROW_HEIGHT = 18f
        private const val ROWS_PER_PAGE = 35
    }

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun export(data: ExportData, outputFile: File) {
        val document = PDDocument()

        // Загрузка правильных шрифтов из папки font
        val regularFont = PDType0Font.load(
            document,
            context.assets.open("font/inter_18pt_regular.ttf"),
        )
        val boldFont = PDType0Font.load(
            document,
            context.assets.open("font/inter_18pt_bold.ttf"),
        )

        var page = PDPage(PDRectangle.A4)
        document.addPage(page)
        var content = PDPageContentStream(document, page)
        var yPosition = PAGE_HEIGHT - MARGIN

        // ── Локализованный Заголовок ────────────────────────
        content.beginText()
        content.setFont(boldFont, 20f)
        content.newLineAtOffset(MARGIN, yPosition)
        content.showText(context.getString(R.string.pdf_report_title))
        content.endText()
        yPosition -= 30f

        content.beginText()
        content.setFont(regularFont, 11f)
        content.newLineAtOffset(MARGIN, yPosition)
        content.showText(context.getString(R.string.pdf_report_period, data.periodLabel))
        content.endText()
        yPosition -= 16f

        content.beginText()
        content.setFont(regularFont, 9f)
        content.newLineAtOffset(MARGIN, yPosition)
        content.showText(context.getString(R.string.pdf_report_generated, formatDate(data.generatedAt)))
        content.endText()
        yPosition -= 30f

        // ── Локализованная Сводная карточка ─────────────────
        content.setNonStrokingColor(245, 245, 247)
        content.addRect(MARGIN, yPosition - 60f, PAGE_WIDTH - 2 * MARGIN, 60f)
        content.fill()

        val summaryY = yPosition - 25f

        // Доходы
        content.beginText()
        content.setFont(regularFont, 10f)
        content.setNonStrokingColor(34, 197, 94)
        content.newLineAtOffset(MARGIN + 16f, summaryY)
        val incomeStr = context.getString(R.string.pdf_report_income, formatMoney(data.totalIncomeMinor, data.currencyCode))
        content.showText(incomeStr)
        content.endText()

        // Расходы
        content.beginText()
        content.setFont(regularFont, 10f)
        content.setNonStrokingColor(255, 59, 48)
        content.newLineAtOffset(MARGIN + 16f, summaryY - 18f)
        val expenseStr = context.getString(R.string.pdf_report_expense, formatMoney(data.totalExpenseMinor, data.currencyCode))
        content.showText(expenseStr)
        content.endText()

        // Баланс
        val balance = data.totalIncomeMinor - data.totalExpenseMinor
        content.beginText()
        content.setFont(boldFont, 11f)
        content.setNonStrokingColor(0, 0, 0)
        content.newLineAtOffset(MARGIN + 260f, summaryY - 9f)
        val balanceStr = context.getString(R.string.pdf_report_balance, formatMoney(balance, data.currencyCode))
        content.showText(balanceStr)
        content.endText()

        yPosition -= 90f

        // ── Локализованная Таблица операций ─────────────────
        val columns = listOf(
            context.getString(R.string.pdf_col_date) to 80f,
            context.getString(R.string.pdf_col_type) to 50f,
            context.getString(R.string.pdf_col_amount) to 70f,
            context.getString(R.string.pdf_col_category) to 110f,
            context.getString(R.string.pdf_col_account) to 80f,
            context.getString(R.string.pdf_col_note) to 90f,
        )

        drawTableHeader(content, boldFont, columns, yPosition)
        yPosition -= ROW_HEIGHT

        var rowCount = 0
        val sortedTx = data.transactions.sortedByDescending { it.date }

        for (tx in sortedTx) {
            if (rowCount >= ROWS_PER_PAGE || yPosition < MARGIN + ROW_HEIGHT) {
                content.close()
                page = PDPage(PDRectangle.A4)
                document.addPage(page)
                content = PDPageContentStream(document, page)
                yPosition = PAGE_HEIGHT - MARGIN
                drawTableHeader(content, boldFont, columns, yPosition)
                yPosition -= ROW_HEIGHT
                rowCount = 0
            }

            if (rowCount % 2 == 1) {
                content.setNonStrokingColor(248, 248, 250)
                content.addRect(MARGIN, yPosition - ROW_HEIGHT, PAGE_WIDTH - 2 * MARGIN, ROW_HEIGHT)
                content.fill()
            }

            drawTableRow(content, regularFont, columns, yPosition, tx)

            yPosition -= ROW_HEIGHT
            rowCount++
        }

        content.close()
        document.save(outputFile)
        document.close()
    }

    private fun drawTableHeader(
        content: PDPageContentStream,
        font: PDFont,
        columns: List<Pair<String, Float>>,
        yPosition: Float,
    ) {
        content.setNonStrokingColor(0, 122, 255)
        content.addRect(MARGIN, yPosition - ROW_HEIGHT, PAGE_WIDTH - 2 * MARGIN, ROW_HEIGHT)
        content.fill()

        content.beginText()
        content.setFont(font, 9f)
        content.setNonStrokingColor(255, 255, 255)
        content.newLineAtOffset(MARGIN + 4f, yPosition - 13f)
        columns.forEachIndexed { i, (title, width) ->
            content.showText(title)
            if (i < columns.lastIndex) content.newLineAtOffset(width, 0f)
        }
        content.endText()
    }

    private fun drawTableRow(
        content: PDPageContentStream,
        font: PDFont,
        columns: List<Pair<String, Float>>,
        yPosition: Float,
        tx: ExportTransaction,
    ) {
        // Проверяем тип операции для раскраски текста
        val isIncome = tx.type == "Income" || tx.type == "Доход"
        val typeLabel = if (isIncome) {
            context.getString(R.string.pdf_type_income)
        } else {
            context.getString(R.string.pdf_type_expense)
        }

        val values = listOf(
            formatDateShort(tx.date),
            typeLabel,
            formatMoney(tx.amountMinor, tx.currencyCode),
            tx.categoryName?.take(15) ?: "—",
            tx.accountName.take(16),
            tx.note?.take(15) ?: "",
        )

        content.beginText()
        content.setFont(font, 8.5f)

        // Установка цвета в зависимости от дохода/расхода
        content.setNonStrokingColor(
            if (isIncome) 34 else 60,
            if (isIncome) 197 else 60,
            if (isIncome) 94 else 60,
        )

        content.newLineAtOffset(MARGIN + 4f, yPosition - 13f)
        values.forEachIndexed { i, value ->
            content.showText(value)
            if (i < values.lastIndex) content.newLineAtOffset(columns[i].second, 0f)
            if (i == 2) content.setNonStrokingColor(0, 0, 0) // после суммы сбрасываем цвет в черный
        }
        content.endText()
    }

    private fun formatMoney(minor: Long, currency: String): String =
        String.format(Locale.getDefault(), "%,.2f %s", minor / 100.0, currency)

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(millis))

    private fun formatDateShort(millis: Long): String =
        SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(millis))
}