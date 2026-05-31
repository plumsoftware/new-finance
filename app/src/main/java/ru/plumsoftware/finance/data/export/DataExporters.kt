package ru.plumsoftware.finance.data.export

import org.json.JSONArray
import org.json.JSONObject
import ru.plumsoftware.finance.domain.model.ExportData
import ru.plumsoftware.finance.domain.model.TransactionType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun export(data: ExportData, cacheDir: File): File {
        val file = File(cacheDir, "finance_export_${System.currentTimeMillis()}.csv")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        buildString {
            if (data.transactions.isNotEmpty()) {
                appendLine("type,amount,category_id,date,note")
                data.transactions.forEach { tx ->
                    appendLine(
                        listOf(
                            tx.type.name,
                            tx.amountMinor.toString(),
                            tx.categoryId?.toString().orEmpty(),
                            dateFormat.format(Date(tx.dateMillis)),
                            tx.note?.replace(',', ' ').orEmpty(),
                        ).joinToString(","),
                    )
                }
            }
            if (data.categories.isNotEmpty()) {
                appendLine()
                appendLine("category_name,type,icon")
                data.categories.forEach { cat ->
                    appendLine("${cat.name},${cat.type.name},${cat.icon}")
                }
            }
            if (data.assets.isNotEmpty()) {
                appendLine()
                appendLine("asset_name,cost,status")
                data.assets.forEach { asset ->
                    appendLine("${asset.name},${asset.purchaseCostMinor},${asset.status.name}")
                }
            }
        }.let { file.writeText(it) }
        return file
    }
}

object JsonExporter {
    fun export(data: ExportData, cacheDir: File): File {
        val root = JSONObject()
        val txArray = JSONArray()
        data.transactions.forEach { tx ->
            txArray.put(
                JSONObject()
                    .put("type", tx.type.name)
                    .put("amountMinor", tx.amountMinor)
                    .put("categoryId", tx.categoryId)
                    .put("dateMillis", tx.dateMillis)
                    .put("note", tx.note),
            )
        }
        root.put("transactions", txArray)

        val catArray = JSONArray()
        data.categories.forEach { cat ->
            catArray.put(
                JSONObject()
                    .put("name", cat.name)
                    .put("type", cat.type.name)
                    .put("icon", cat.icon),
            )
        }
        root.put("categories", catArray)

        val assetArray = JSONArray()
        data.assets.forEach { asset ->
            assetArray.put(
                JSONObject()
                    .put("name", asset.name)
                    .put("purchaseCostMinor", asset.purchaseCostMinor)
                    .put("status", asset.status.name),
            )
        }
        root.put("assets", assetArray)
        root.put("currencyCode", data.currencyCode)

        val file = File(cacheDir, "finance_export_${System.currentTimeMillis()}.json")
        file.writeText(root.toString(2))
        return file
    }
}

object PdfExporter {
    fun export(data: ExportData, cacheDir: File): File {
        val file = File(cacheDir, "finance_export_${System.currentTimeMillis()}.pdf")
        val document = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply { textSize = 12f }
        var y = 40f
        canvas.drawText("Finance Export", 40f, y, paint.apply { textSize = 18f })
        y += 30f
        paint.textSize = 12f

        data.transactions.take(40).forEach { tx ->
            val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
            canvas.drawText(
                "$sign${tx.amountMinor / 100.0} ${tx.note.orEmpty()}",
                40f,
                y,
                paint,
            )
            y += 18f
        }
        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }
}
