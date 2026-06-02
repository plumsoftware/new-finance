package ru.plumsoftware.finance.domain.model

import androidx.annotation.StringRes
import ru.plumsoftware.finance.R

const val BACKUP_FORMAT_VERSION = 4

enum class ExportFormat(
    @StringRes val labelRes: Int,
    val mimeType: String,
    val extension: String,
) {
    BACKUP(R.string.export_format_backup, "application/json", "owlbackup"),
    JSON(R.string.export_format_json, "application/json", "json"),
}

enum class ExportPeriod(@StringRes val labelRes: Int) {
    THIS_MONTH(R.string.period_this_month),
    LAST_3_MONTHS(R.string.period_last_3m),
    THIS_YEAR(R.string.period_this_year),
    ALL_TIME(R.string.period_all_time),
    CUSTOM(R.string.period_custom),
}

enum class ImportStrategy(@StringRes val titleRes: Int, @StringRes val descRes: Int) {
    MERGE(R.string.strategy_merge_title, R.string.strategy_merge_desc),
    REPLACE(R.string.strategy_replace_title, R.string.strategy_replace_desc),
    OVERWRITE(R.string.strategy_overwrite_title, R.string.strategy_overwrite_desc),
}

data class BackupRecordCounts(
    val categories: Int = 0,
    val transactions: Int = 0,
    val recurring: Int = 0,
    val assets: Int = 0,
    val limits: Int = 0,
    val goals: Int = 0,
    val goalDeposits: Int = 0,
    val achievements: Int = 0,
)

data class BackupMeta(
    val version: Int = BACKUP_FORMAT_VERSION,
    val appVersion: String = "",
    val exportedAt: String = "",
    val deviceModel: String = "",
    val recordCounts: BackupRecordCounts = BackupRecordCounts(),
    val fileName: String = "",
)

data class BackupCategoryDto(
    val id: Long,
    val name: String,
    val emoji: String,
    val colorHex: String?,
    val isIncome: Boolean,
    val sortOrder: Int,
)

data class BackupTransactionDto(
    val id: Long,
    val amount: Double,
    val categoryId: Long?,
    val isIncome: Boolean,
    val date: String,
    val note: String?,
    val smartAssetId: Long? = null,
)

data class BackupRecurringDto(
    val id: Long,
    val title: String,
    val amount: Double,
    val categoryId: Long,
    val isIncome: Boolean,
    val frequency: String,
    val dayOfMonth: Int?,
    val nextDate: String,
    val isActive: Boolean,
    val note: String?,
)

data class BackupAssetDto(
    val id: Long,
    val name: String,
    val emoji: String,
    val purchaseCost: Double,
    val savingsPerUse: Double,
    val totalSaved: Double,
    val usageCount: Int,
    val createdAt: String,
    val isActive: Boolean,
    val note: String?,
    val trackingMode: String,
    val status: String,
)

data class BackupAssetUsageDto(
    val assetId: Long,
    val date: String,
    val savedAmount: Double,
    val note: String? = null,
)

data class BackupLimitDto(
    val categoryId: Long,
    val monthlyLimit: Double,
)

data class BackupGoalDto(
    val id: Long,
    val name: String,
    val emoji: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val colorHex: String,
    val deadlineMillis: Long?,
    val note: String?,
    val showOnHome: Boolean,
    val isCompleted: Boolean,
    val createdAtMillis: Long,
)

data class BackupGoalDepositDto(
    val id: Long,
    val goalId: Long,
    val amount: Double,
    val note: String?,
    val createdAtMillis: Long,
)

data class BackupAchievementUnlockDto(
    val key: String,
    val unlockedAtMillis: Long,
)

data class BackupModel(
    val meta: BackupMeta = BackupMeta(),
    val categories: List<BackupCategoryDto> = emptyList(),
    val transactions: List<BackupTransactionDto> = emptyList(),
    val recurringTransactions: List<BackupRecurringDto> = emptyList(),
    val assets: List<BackupAssetDto> = emptyList(),
    val assetUsageHistory: List<BackupAssetUsageDto> = emptyList(),
    val limits: List<BackupLimitDto> = emptyList(),
    val goals: List<BackupGoalDto> = emptyList(),
    val goalDeposits: List<BackupGoalDepositDto> = emptyList(),
    val achievements: List<BackupAchievementUnlockDto> = emptyList(),
)

data class ImportResult(
    val added: Int,
    val updated: Int,
    val skipped: Int,
)

sealed class ImportState {
    data object Parsing : ImportState()
    data class Error(val message: String) : ImportState()
    data class Preview(
        val meta: BackupMeta,
        val backup: BackupModel,
        val strategy: ImportStrategy = ImportStrategy.MERGE,
    ) : ImportState()
    data class Importing(val progress: Float) : ImportState()
    data class Success(
        val added: Int,
        val updated: Int,
        val skipped: Int,
    ) : ImportState()
}

sealed class ExportState {
    data object Idle : ExportState()
    data object Loading : ExportState()
    data class Success(
        val fileName: String,
        val shareUri: android.net.Uri?,
    ) : ExportState()
    data class Error(val message: String) : ExportState()
}
