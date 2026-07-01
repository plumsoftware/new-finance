package ru.plumsoftware.finance.domain.model

import android.content.Context
import androidx.annotation.StringRes
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import java.util.Calendar
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val BACKUP_FORMAT_VERSION = 5

enum class ExportFormat(
    @StringRes val labelRes: Int,
    val mimeType: String,
    val extension: String,
) {
    BACKUP(R.string.export_format_backup, "application/json", "owlbackup"),
    JSON(R.string.export_format_json, "application/json", "json"),
    PDF(
        labelRes = R.string.export_format_pdf,
        mimeType = "application/pdf",
        extension = "pdf",
    ),
    XLSX(
        labelRes = R.string.export_format_xlsx,
        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        extension = "xlsx",
    ),
    CSV(
        labelRes = R.string.export_format_csv,
        mimeType = "text/csv",
        extension = "csv",
    ),
}

enum class ExportPeriod(@StringRes val labelRes: Int) {
    THIS_MONTH(R.string.period_this_month),
    LAST_3_MONTHS(R.string.period_last_3m),
    THIS_YEAR(R.string.period_this_year),
    ALL_TIME(R.string.period_all_time),
    CUSTOM(R.string.period_custom),
}

class ExportDataBuilder(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
) {

    suspend fun build(
        period: ExportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
        currencyCode: String,
    ): ExportData {
        val (from, to) = resolvePeriodRange(period, customStartMillis, customEndMillis)

        val transactions = transactionRepository.observeByPeriod(from, to).first()
        val accountsWithBalances = accountRepository.observeAllWithBalances().first()

        val incomeCategories = categoryRepository.observeByType(CategoryType.INCOME, includeHidden = true).first()
        val expenseCategories = categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true).first()
        val categories = incomeCategories + expenseCategories

        val exportTransactions = transactions.map { tx ->
            ExportTransaction(
                date = tx.dateMillis,
                type = if (tx.type == TransactionType.INCOME) "Income" else "Expense",
                amountMinor = tx.amountMinor,
                currencyCode = tx.currencyCode,
                originalAmountMinor = tx.originalAmountMinor,
                originalCurrencyCode = tx.originalCurrencyCode,
                categoryName = categories.find { it.id == tx.categoryId }?.name,
                accountName = accountsWithBalances.find { it.account.id == tx.accountId }?.account?.name ?: context.getString(R.string.pdf_main_account),
                note = tx.note,
            )
        }

        val totalIncome = exportTransactions.filter { it.type == "Income" }.sumOf { it.amountMinor }
        val totalExpense = exportTransactions.filter { it.type == "Expense" }.sumOf { it.amountMinor }

        return ExportData(
            periodLabel = formatPeriodLabel(period, from, to),
            generatedAt = System.currentTimeMillis(),
            accounts = accountsWithBalances.map { accWithBal ->
                ExportAccount(
                    name = accWithBal.account.name,
                    type = accWithBal.account.type.toString(),
                    currencyCode = accWithBal.account.currencyCode,
                    balanceMinor = accWithBal.calculatedBalanceMinor,
                )
            },
            transactions = exportTransactions,
            totalIncomeMinor = totalIncome,
            totalExpenseMinor = totalExpense,
            currencyCode = currencyCode,
        )
    }

    private fun resolvePeriodRange(
        period: ExportPeriod,
        customStart: Long?,
        customEnd: Long?,
    ): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        return when (period) {
            ExportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                startOfDay(cal.timeInMillis) to System.currentTimeMillis()
            }
            ExportPeriod.LAST_3_MONTHS -> {
                cal.add(Calendar.MONTH, -3)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = startOfDay(cal.timeInMillis)
                cal.add(Calendar.MONTH, 3)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val end = startOfDay(cal.timeInMillis) - 1
                start to end
            }
            ExportPeriod.ALL_TIME -> 0L to System.currentTimeMillis()
            ExportPeriod.CUSTOM -> (customStart ?: 0L) to (customEnd ?: System.currentTimeMillis())
            ExportPeriod.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                startOfDay(cal.timeInMillis) to System.currentTimeMillis()
            }
        }
    }

    private fun formatPeriodLabel(period: ExportPeriod, from: Long, to: Long): String = when (period) {
        ExportPeriod.ALL_TIME -> context.getString(R.string.period_all_time) // Локализованная строка
        ExportPeriod.CUSTOM -> {
            val startStr = formatDate(from, "d MMM")
            val endStr = formatDate(to, "d MMM yyyy")
            context.getString(R.string.date_range_label, startStr, endStr)
        }
        else -> formatDate(from, "LLLL yyyy")
    }

    private fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun formatDate(millis: Long, pattern: String): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis)) // Локаль устройства
}

enum class ImportStrategy(@StringRes val titleRes: Int, @StringRes val descRes: Int) {
    MERGE(R.string.strategy_merge_title, R.string.strategy_merge_desc),
    REPLACE(R.string.strategy_replace_title, R.string.strategy_replace_desc),
    OVERWRITE(R.string.strategy_overwrite_title, R.string.strategy_overwrite_desc),
}

data class BackupRecordCounts(
    val accounts: Int = 0,
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

data class BackupAccountDto(
    val id: Long,
    val name: String,
    val type: String,
    val currencyCode: String,
    val colorHex: String,
    val emoji: String,
    val initialBalance: Double,
    val sortOrder: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAtMillis: Long,
)

data class BackupTransactionDto(
    val id: Long,
    val amount: Double,
    val categoryId: Long?,
    val isIncome: Boolean,
    val date: String,
    val note: String?,
    val smartAssetId: Long? = null,
    val accountId: Long = 1L,
    val accountName: String? = null,
    val currencyCode: String = "RUB",
    val originalAmount: Double = 0.0,
    val originalCurrencyCode: String? = null,
    val exchangeRate: Double = 1.0,
    val type: String? = null,
    val goalId: Long? = null,
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
    val currencyCode: String = "RUB",
    val accountId: Long? = null,
)

data class BackupGoalDepositDto(
    val id: Long,
    val goalId: Long,
    val amount: Double,
    val note: String?,
    val createdAtMillis: Long,
    val currencyCode: String = "RUB",
    val accountId: Long? = null,
    val transactionId: Long? = null,
)

data class BackupAchievementUnlockDto(
    val key: String,
    val unlockedAtMillis: Long,
)

data class BackupModel(
    val meta: BackupMeta = BackupMeta(),
    val accounts: List<BackupAccountDto> = emptyList(),
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
