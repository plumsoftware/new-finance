package ru.plumsoftware.finance.data.report

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import java.io.File
import java.time.LocalDate

data class ReportFile(val file: File, val uri: Uri, val fileName: String, val mimeType: String)

/** Формирование файла отчёта/экспорта в `cache/exports` (§6.6, §9). */
class ReportService(
    private val context: Context,
    private val builder: ReportBuilder,
    private val settingsRepository: SettingsRepository,
) {
    private val csv = CsvReportWriter(context)
    private val xlsx = XlsxReportWriter(context)
    private val pdf = PdfReportWriter(context)

    fun fileName(request: ReportRequest): String = ReportFileNames.build(
        kind = request.kind,
        format = request.format,
        range = request.range,
        isWholeMonth = request.isWholeMonth,
        today = LocalDate.now(),
        reportPrefix = context.getString(R.string.report_file_prefix),
        exportPrefix = context.getString(R.string.report_export_file_prefix),
        monthYear = DateFmt::monthYear,
        rangeLabel = DateFmt::range,
        numericDate = DateFmt::numeric,
    )

    suspend fun generate(request: ReportRequest): ReportFile = withContext(Dispatchers.IO) {
        val report = builder.build(request)
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val name = fileName(request)
        val file = File(dir, name)
        when (request.format) {
            ReportFormat.CSV -> csv.write(report, file)
            ReportFormat.XLSX -> xlsx.write(report, file)
            ReportFormat.PDF -> pdf.write(report, file)
        }
        if (request.kind == ReportKind.REPORT) {
            settingsRepository.update { it.copy(reportsExported = it.reportsExported + 1) }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        ReportFile(file, uri, name, request.format.mimeType)
    }

    /** Копирование в выбранное через SAF место («Загрузки»). */
    suspend fun copyTo(source: File, target: Uri) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(target)?.use { out -> source.inputStream().use { it.copyTo(out) } }
            ?: error("Cannot open $target")
    }
}
