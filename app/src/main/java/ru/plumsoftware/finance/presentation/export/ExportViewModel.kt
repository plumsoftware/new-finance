package ru.plumsoftware.finance.presentation.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.plumsoftware.finance.data.backup.BackupFileWriter
import ru.plumsoftware.finance.domain.model.ExportFormat
import ru.plumsoftware.finance.domain.model.ExportPeriod
import ru.plumsoftware.finance.domain.model.ExportState
import ru.plumsoftware.finance.domain.repository.BackupRepository

class ExportViewModel(
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState = _exportState.asStateFlow()

    fun saveBackup(
        format: ExportFormat,
        period: ExportPeriod,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null,
    ) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    performExport(format, period, customStartMillis, customEndMillis)
                }
                _exportState.value = ExportState.Success(
                    fileName = result.first,
                    shareUri = null,
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message.orEmpty())
            }
        }
    }

    fun exportAndShare(
        format: ExportFormat,
        period: ExportPeriod,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null,
    ) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    performExport(format, period, customStartMillis, customEndMillis)
                }
                _exportState.value = ExportState.Success(
                    fileName = result.first,
                    shareUri = result.second,
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message.orEmpty())
            }
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }

    private suspend fun performExport(
        format: ExportFormat,
        period: ExportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
    ): Pair<String, Uri> {
        val backup = backupRepository.buildBackup(
            period = period,
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis,
        )
        val fileName = BackupFileWriter.buildFileName(format)
        val uri = backupRepository.exportToUri(backup, format)
        return fileName to uri
    }
}
