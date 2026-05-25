package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.first
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.domain.model.PeriodSummary
import ru.plumsoftware.finance.domain.model.SavingsIndex
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.AnalyticsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import java.util.concurrent.TimeUnit

class AnalyticsRepositoryImpl(
    private val transactionRepository: TransactionRepository,
    private val smartAssetDao: SmartAssetDao,
) : AnalyticsRepository {

    override suspend fun getPeriodSummary(startMillis: Long, endMillis: Long): PeriodSummary {
        val income = transactionRepository.sumByTypeInPeriod(
            TransactionType.INCOME,
            startMillis,
            endMillis,
        )
        val expense = transactionRepository.sumByTypeInPeriod(
            TransactionType.EXPENSE,
            startMillis,
            endMillis,
        )
        val savings = transactionRepository.sumByTypeInPeriod(
            TransactionType.SAVINGS,
            startMillis,
            endMillis,
        )
        return PeriodSummary(
            incomeMinor = income,
            expenseMinor = expense,
            savingsMinor = savings,
            netMinor = income - expense,
        )
    }

    override suspend fun getSavingsIndex(startMillis: Long, endMillis: Long): SavingsIndex {
        val totalSmartSavings = smartAssetDao.observeTotalSavedAllTime().first()
        val periodDays = ((endMillis - startMillis).coerceAtLeast(1L) / TimeUnit.DAYS.toMillis(1))
            .coerceAtLeast(1L)
        val estimatedMonthly = totalSmartSavings * 30L / periodDays
        return SavingsIndex(
            totalSmartSavingsMinor = totalSmartSavings,
            estimatedMonthlyReductionMinor = estimatedMonthly,
        )
    }
}
