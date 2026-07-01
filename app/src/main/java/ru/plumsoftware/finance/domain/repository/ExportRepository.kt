package ru.plumsoftware.finance.domain.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import ru.plumsoftware.finance.domain.model.CsvExporter
import ru.plumsoftware.finance.domain.model.ExportDataBuilder
import ru.plumsoftware.finance.domain.model.ExportFormat
import ru.plumsoftware.finance.domain.model.ExportPeriod
import ru.plumsoftware.finance.domain.model.PdfExporter
import ru.plumsoftware.finance.domain.model.XlsxExporter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportRepository(
    private val context: Context,
    private val exportDataBuilder: ExportDataBuilder,
    private val csvExporter: CsvExporter,
    private val xlsxExporter: XlsxExporter,
    private val pdfExporter: PdfExporter,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun exportToFile(
        format: ExportFormat,
        period: ExportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
    ): Pair<String, Uri> = withContext(Dispatchers.IO) {

        require(format != ExportFormat.BACKUP && format != ExportFormat.JSON) {
            "BACKUP и JSON форматы обрабатываются через BackupRepository"
        }

        val currencyCode = settingsRepository.settings.first().defaultCurrencyCode
        val data = exportDataBuilder.build(period, customStartMillis, customEndMillis, currencyCode)

        val fileName = buildFileName(format)
        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)

        when (format) {
            ExportFormat.CSV -> outputFile.writeText(csvExporter.export(data), Charsets.UTF_8)
            ExportFormat.XLSX -> xlsxExporter.export(data, outputFile)
            ExportFormat.PDF -> pdfExporter.export(data, outputFile)
            ExportFormat.BACKUP, ExportFormat.JSON -> error("unreachable")
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile,
        )

        fileName to uri
    }

    private fun buildFileName(format: ExportFormat): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
        return "export_$timestamp.${format.extension}"
    }
}

/**
 * Чистит старые файлы экспорта из cache/exports/, чтобы не копились мусором.
 * Вызови ExportCacheCleaner(context).cleanOldExports() один раз
 * в Application.onCreate() своего класса (или в первом экране при старте).
 */
class ExportCacheCleaner(private val context: Context) {
    fun cleanOldExports() {
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) return

        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        exportsDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) file.delete()
        }
    }
}