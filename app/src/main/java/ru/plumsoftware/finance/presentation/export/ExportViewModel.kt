package ru.plumsoftware.finance.presentation.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.data.export.CsvExporter
import ru.plumsoftware.finance.data.export.JsonExporter
import ru.plumsoftware.finance.data.export.PdfExporter
import ru.plumsoftware.finance.domain.model.ExportFormat
import ru.plumsoftware.finance.domain.model.ExportOptions
import ru.plumsoftware.finance.domain.model.ExportPeriod
import ru.plumsoftware.finance.domain.model.ExportState
import ru.plumsoftware.finance.domain.repository.ExportRepository

class ExportViewModel(
    private val repository: ExportRepository,
    private val context: Context,
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState = _exportState.asStateFlow()

    fun export(
        format: ExportFormat,
        period: ExportPeriod,
        include: ExportOptions,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null,
    ) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val data = repository.getExportData(
                    period = period,
                    include = include,
                    customStartMillis = customStartMillis,
                    customEndMillis = customEndMillis,
                )
                val file = when (format) {
                    ExportFormat.CSV -> CsvExporter.export(data, context.cacheDir)
                    ExportFormat.JSON -> JsonExporter.export(data, context.cacheDir)
                    ExportFormat.PDF -> PdfExporter.export(data, context.cacheDir)
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                _exportState.value = ExportState.Success(uri)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message)
            }
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
