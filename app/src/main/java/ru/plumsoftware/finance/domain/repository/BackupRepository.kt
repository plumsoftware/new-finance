package ru.plumsoftware.finance.domain.repository

import android.net.Uri
import ru.plumsoftware.finance.domain.model.BackupModel
import ru.plumsoftware.finance.domain.model.ExportFormat
import ru.plumsoftware.finance.domain.model.ExportPeriod
import ru.plumsoftware.finance.domain.model.ImportResult
import ru.plumsoftware.finance.domain.model.ImportStrategy

interface BackupRepository {
    suspend fun buildBackup(
        period: ExportPeriod,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null,
    ): BackupModel

    suspend fun exportToUri(
        backup: BackupModel,
        format: ExportFormat,
    ): Uri

    suspend fun parseFromUri(uri: Uri, fileName: String): BackupModel

    fun parseFromJson(json: String, fileName: String = ""): BackupModel

    suspend fun importBackup(
        backup: BackupModel,
        strategy: ImportStrategy,
        onProgress: (Float) -> Unit = {},
    ): ImportResult
}
