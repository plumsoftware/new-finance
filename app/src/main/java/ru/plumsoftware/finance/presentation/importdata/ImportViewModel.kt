package ru.plumsoftware.finance.presentation.importdata

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.ImportState
import ru.plumsoftware.finance.domain.model.ImportStrategy
import ru.plumsoftware.finance.domain.repository.BackupRepository
import ru.plumsoftware.finance.util.ImportFileHelper

class ImportViewModel(
    private val backupRepository: BackupRepository,
    private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow<ImportState>(ImportState.Parsing)
    val state = _state.asStateFlow()

    fun parseFile(absolutePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = ImportState.Parsing

            val json = ImportFileHelper.readTempFile(absolutePath)
            if (json == null) {
                _state.value = ImportState.Error(
                    context.getString(R.string.import_error_cannot_open),
                )
                return@launch
            }

            try {
                val backup = backupRepository.parseFromJson(
                    json,
                    File(absolutePath).name,
                )
                _state.value = ImportState.Preview(
                    meta = backup.meta.copy(
                        fileName = File(absolutePath).name.ifBlank { backup.meta.fileName },
                    ),
                    backup = backup,
                )
            } catch (_: Exception) {
                _state.value = ImportState.Error(
                    context.getString(R.string.import_error_not_our_format),
                )
            }
        }
    }

    fun setStrategy(strategy: ImportStrategy) {
        val current = _state.value as? ImportState.Preview ?: return
        _state.value = current.copy(strategy = strategy)
    }

    fun import(preview: ImportState.Preview, absolutePath: String) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    backupRepository.importBackup(
                        backup = preview.backup,
                        strategy = preview.strategy,
                        onProgress = { progress ->
                            _state.value = ImportState.Importing(progress)
                        },
                    )
                }
                ImportFileHelper.deleteTempFile(context, absolutePath)
                _state.value = ImportState.Success(
                    added = result.added,
                    updated = result.updated,
                    skipped = result.skipped,
                )
            } catch (e: Exception) {
                _state.value = ImportState.Error(
                    e.message ?: context.getString(R.string.import_error_unknown),
                )
            }
        }
    }
}
