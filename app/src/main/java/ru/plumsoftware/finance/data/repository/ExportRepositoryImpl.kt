package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.first
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.ExportData
import ru.plumsoftware.finance.domain.model.ExportOptions
import ru.plumsoftware.finance.domain.model.ExportPeriod
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.ExportRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import java.util.Calendar

class ExportRepositoryImpl(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val smartAssetRepository: SmartAssetRepository,
    private val settingsRepository: SettingsRepository,
) : ExportRepository {

    override suspend fun getExportData(
        period: ExportPeriod,
        include: ExportOptions,
        customStartMillis: Long?,
        customEndMillis: Long?,
    ): ExportData {
        val settings = settingsRepository.settings.first()
        val (startMillis, endMillis) = resolvePeriodMillis(period, customStartMillis, customEndMillis)

        val transactions = if (include.transactions) {
            transactionRepository.observeByPeriod(startMillis, endMillis).first()
        } else {
            emptyList()
        }
        val expenseCategories = if (include.categories) {
            categoryRepository.observeByType(
                ru.plumsoftware.finance.domain.model.CategoryType.EXPENSE,
                includeHidden = true,
            ).first()
        } else {
            emptyList()
        }
        val incomeCategories = if (include.categories) {
            categoryRepository.observeByType(
                ru.plumsoftware.finance.domain.model.CategoryType.INCOME,
                includeHidden = true,
            ).first()
        } else {
            emptyList()
        }
        val assets = if (include.assets) {
            smartAssetRepository.observeActive().first()
        } else {
            emptyList()
        }

        return ExportData(
            transactions = transactions,
            categories = expenseCategories + incomeCategories,
            assets = assets,
            currencyCode = settings.defaultCurrencyCode,
        )
    }

    private fun resolvePeriodMillis(
        period: ExportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
    ): Pair<Long, Long> {
        if (period == ExportPeriod.CUSTOM && customStartMillis != null && customEndMillis != null) {
            return startOfDayMillis(customStartMillis) to endOfDayMillis(customEndMillis)
        }
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        return when (period) {
            ExportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = startOfDayMillis(cal.timeInMillis)
                cal.add(Calendar.MONTH, 1)
                start to cal.timeInMillis
            }
            ExportPeriod.LAST_3_MONTHS -> {
                cal.add(Calendar.MONTH, -3)
                startOfDayMillis(cal.timeInMillis) to now
            }
            ExportPeriod.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = startOfDayMillis(cal.timeInMillis)
                cal.add(Calendar.YEAR, 1)
                start to cal.timeInMillis
            }
            ExportPeriod.ALL_TIME -> 0L to Long.MAX_VALUE
            ExportPeriod.CUSTOM -> startOfDayMillis(now - 30L * 86_400_000L) to now
        }
    }
}
