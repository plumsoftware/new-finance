package ru.plumsoftware.finance.data.report

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextUtils
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import ru.plumsoftware.finance.R
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * PDF-отчёт (§9): A4 книжная, Inter. Шапка с логотипом и Коппи, итоги, график, категории,
 * операции; в подвале — исключённые категории и нумерация страниц.
 */
class PdfReportWriter(private val context: Context) {

    private val pageW = 595
    private val pageH = 842
    private val margin = 40f
    private val contentW get() = pageW - margin * 2
    private val footerH = 36f

    private val regular: Typeface = runCatching { ResourcesCompat.getFont(context, R.font.inter_18pt_regular) }.getOrNull() ?: Typeface.DEFAULT
    private val semibold: Typeface = runCatching { ResourcesCompat.getFont(context, R.font.inter_18pt_semibold) }.getOrNull() ?: Typeface.DEFAULT_BOLD
    private val bold: Typeface = runCatching { ResourcesCompat.getFont(context, R.font.inter_24pt_bold) }.getOrNull() ?: Typeface.DEFAULT_BOLD

    private val ink = 0xFF1C1C1E.toInt()
    private val secondary = 0xFF6E6E76.toInt()
    private val divider = 0xFFE5E5EA.toInt()
    private val primary = 0xFF007AFF.toInt()
    private val barDefault = 0xFFB9CFF7.toInt()
    private val warning = 0xFFFF9500.toInt()
    private val success = 0xFF248A3D.toInt()
    private val danger = 0xFFD70015.toInt()

    private fun paint(size: Float, face: Typeface = regular, color: Int = ink) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        typeface = face
        this.color = color
    }

    /** Блок контента: высота и отрисовка с верхней координатой. */
    private class Block(val height: Float, val keepHeaderOnBreak: Block? = null, val draw: (Canvas, Float) -> Unit)

    fun write(report: Report, file: File) {
        val blocks = buildBlocks(report)
        // Раскладка по страницам.
        val pages = mutableListOf<MutableList<Pair<Block, Float>>>()
        var current = mutableListOf<Pair<Block, Float>>()
        var y = margin
        val bottom = pageH - margin - footerH
        blocks.forEach { b ->
            if (y + b.height > bottom && current.isNotEmpty()) {
                pages += current
                current = mutableListOf()
                y = margin
                b.keepHeaderOnBreak?.let { h ->
                    current += h to y
                    y += h.height
                }
            }
            current += b to y
            y += b.height
        }
        if (current.isNotEmpty()) pages += current

        val doc = PdfDocument()
        pages.forEachIndexed { index, items ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, index + 1).create())
            val canvas = page.canvas
            items.forEach { (block, top) -> block.draw(canvas, top) }
            drawFooter(canvas, report, index + 1, pages.size)
            doc.finishPage(page)
        }
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
    }

    private fun drawFooter(canvas: Canvas, report: Report, page: Int, total: Int) {
        val y = pageH - margin + 4f
        val small = paint(8.5f, color = secondary)
        if (report.excludedNames.isNotEmpty()) {
            val text = context.getString(R.string.report_excluded, report.excludedNames.joinToString(", "))
            canvas.drawText(TextUtils.ellipsize(text, small, contentW - 80f, TextUtils.TruncateAt.END).toString(), margin, y, small)
        }
        val pageText = context.getString(R.string.report_page, page, total)
        canvas.drawText(pageText, pageW - margin - small.measureText(pageText), y, small)
    }

    private fun buildBlocks(r: Report): List<Block> {
        val blocks = mutableListOf<Block>()
        val cur = r.currencyCode

        // 1. Шапка.
        val logo = decode(R.drawable.icon, 44)
        val kopi = decode(R.drawable.mascot_happy, 56)
        blocks += Block(78f) { c, top ->
            c.drawText(r.title, margin, top + 22f, paint(20f, bold))
            c.drawText(r.periodLabel.replaceFirstChar { it.titlecase() }, margin, top + 42f, paint(12f, semibold, secondary))
            val generated = context.getString(
                R.string.report_generated,
                r.generatedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            )
            c.drawText(generated, margin, top + 58f, paint(9f, color = secondary))
            kopi?.let { c.drawBitmap(it, pageW - margin - it.width, top, null) }
            logo?.let { c.drawBitmap(it, pageW - margin - (kopi?.width ?: 0) - 8f - it.width, top + 6f, null) }
            line(c, top + 72f)
        }

        // 2. Итоги.
        blocks += Block(70f) { c, top ->
            val colW = contentW / 4
            val compare = r.comparePercent
            val items = listOf(
                Triple(context.getString(R.string.report_expenses), pdfMoney(r.totalExpenseMinor, cur), ink),
                Triple(context.getString(R.string.report_incomes), pdfMoney(r.totalIncomeMinor, cur), success),
                Triple(context.getString(R.string.report_diff), pdfMoney(r.diffMinor, cur), if (r.diffMinor >= 0) success else danger),
                Triple(
                    context.getString(R.string.report_compare),
                    compare?.let { "${if (it > 0) "↑ +" else if (it < 0) "↓ " else ""}$it%" } ?: "—",
                    when {
                        compare == null -> secondary
                        compare > 0 -> danger
                        else -> success
                    },
                ),
            )
            items.forEachIndexed { i, (label, value, color) ->
                val x = margin + colW * i
                c.drawText(label, x, top + 22f, paint(9f, color = secondary))
                c.drawText(value, x, top + 42f, paint(14f, bold, color))
            }
            line(c, top + 60f)
        }

        // 3. График.
        if (r.request.includeChart && r.bars.isNotEmpty()) {
            blocks += Block(190f) { c, top ->
                c.drawText(context.getString(R.string.report_section_chart), margin, top + 18f, paint(12f, semibold))
                val chartTop = top + 32f
                val chartH = 120f
                val max = r.bars.maxOf { it.valueMinor }.coerceAtLeast(1)
                val avg = r.bars.filter { it.valueMinor > 0 }.map { it.valueMinor }.average().takeIf { !it.isNaN() } ?: 0.0
                val gap = 2f
                val w = (contentW - gap * (r.bars.size - 1)) / r.bars.size
                val p = Paint(Paint.ANTI_ALIAS_FLAG)
                val axis = paint(7.5f, color = secondary)
                r.bars.forEachIndexed { i, b ->
                    val h = (b.valueMinor.toFloat() / max * chartH).coerceAtLeast(2f)
                    val x = margin + i * (w + gap)
                    p.color = when {
                        b.isCurrent -> primary
                        b.valueMinor > avg * 1.25 && avg > 0 -> warning
                        b.valueMinor <= 0 -> divider
                        else -> barDefault
                    }
                    c.drawRoundRect(RectF(x, chartTop + chartH - h, x + w, chartTop + chartH), 3f, 3f, p)
                    if (b.label.isNotEmpty()) {
                        c.drawText(b.label, x + w / 2 - axis.measureText(b.label) / 2, chartTop + chartH + 12f, axis)
                    }
                }
                line(c, top + 182f)
            }
        }

        // 4. Категории.
        if (r.request.includeCategories && r.categories.isNotEmpty()) {
            blocks += Block(30f) { c, top -> c.drawText(context.getString(R.string.report_section_categories), margin, top + 20f, paint(12f, semibold)) }
            val headerRow = Block(20f) { c, top ->
                val hp = paint(8.5f, semibold, secondary)
                c.drawText(context.getString(R.string.report_col_category), margin, top + 13f, hp)
                val sum = context.getString(R.string.report_col_amount)
                c.drawText(sum, margin + contentW * 0.72f - hp.measureText(sum), top + 13f, hp)
                val share = context.getString(R.string.report_col_share)
                c.drawText(share, margin + contentW - hp.measureText(share), top + 13f, hp)
            }
            blocks += headerRow
            val maxShare = r.categories.maxOf { it.share }.coerceAtLeast(0.0001)
            r.categories.forEach { cat ->
                blocks += Block(30f, keepHeaderOnBreak = headerRow) { c, top ->
                    val text = paint(10f)
                    c.drawText(
                        TextUtils.ellipsize("${cat.emoji} ${cat.name}", text, contentW * 0.45f, TextUtils.TruncateAt.END).toString(),
                        margin, top + 13f, text,
                    )
                    val sum = pdfMoney(cat.amountMinor, cur)
                    val sp = paint(10f, semibold)
                    c.drawText(sum, margin + contentW * 0.72f - sp.measureText(sum), top + 13f, sp)
                    val share = "${"%.1f".format(cat.share * 100).replace('.', ',')}%"
                    c.drawText(share, margin + contentW - text.measureText(share), top + 13f, text)
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = divider }
                    c.drawRoundRect(RectF(margin, top + 19f, margin + contentW, top + 23f), 2f, 2f, p)
                    p.color = cat.colorArgb or 0xFF000000.toInt()
                    c.drawRoundRect(RectF(margin, top + 19f, margin + (contentW * (cat.share / maxShare)).toFloat(), top + 23f), 2f, 2f, p)
                }
            }
            blocks += Block(10f) { c, top -> line(c, top + 4f) }
        }

        // 5. Операции.
        if (r.request.includeOperations && r.operations.isNotEmpty()) {
            blocks += Block(30f) { c, top -> c.drawText(context.getString(R.string.report_section_operations), margin, top + 20f, paint(12f, semibold)) }
            val cols = floatArrayOf(0f, 0.15f, 0.47f, 0.69f, 1f)
            val header = Block(20f) { c, top ->
                val hp = paint(8.5f, semibold, secondary)
                listOf(R.string.report_col_date, R.string.report_col_title, R.string.report_col_category, R.string.report_col_account)
                    .forEachIndexed { i, res -> c.drawText(context.getString(res), margin + contentW * cols[i], top + 13f, hp) }
                val amount = context.getString(R.string.report_col_amount)
                c.drawText(amount, margin + contentW - hp.measureText(amount), top + 13f, hp)
                line(c, top + 18f)
            }
            blocks += header
            val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
            r.operations.forEach { op ->
                blocks += Block(18f, keepHeaderOnBreak = header) { c, top ->
                    val t = paint(8.5f)
                    fun cell(text: String, i: Int) {
                        val w = contentW * (cols[i + 1] - cols[i]) - 6f
                        c.drawText(TextUtils.ellipsize(text, t, w, TextUtils.TruncateAt.END).toString(), margin + contentW * cols[i], top + 12f, t)
                    }
                    cell(op.dateTime.format(dateFmt), 0)
                    cell(op.title, 1)
                    cell(op.category, 2)
                    val amount = (if (op.isIncome) "+" else "−") + pdfMoney(op.amountMinor, cur)
                    val ap = paint(8.5f, semibold, if (op.isIncome) success else ink)
                    val accW = contentW * (1f - cols[3]) - ap.measureText(amount) - 10f
                    c.drawText(TextUtils.ellipsize(op.account, t, accW, TextUtils.TruncateAt.END).toString(), margin + contentW * cols[3], top + 12f, t)
                    c.drawText(amount, margin + contentW - ap.measureText(amount), top + 12f, ap)
                }
            }
        }
        return blocks
    }

    private fun line(c: Canvas, y: Float) {
        c.drawLine(margin, y, pageW - margin, y, Paint().apply { color = divider; strokeWidth = 0.8f })
    }

    private fun decode(res: Int, size: Int): Bitmap? = runCatching {
        val src = BitmapFactory.decodeResource(context.resources, res) ?: return null
        Bitmap.createScaledBitmap(src, size, size, true)
    }.getOrNull()
}
