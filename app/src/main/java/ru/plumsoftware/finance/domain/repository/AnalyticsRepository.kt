package ru.plumsoftware.finance.domain.repository

import ru.plumsoftware.finance.domain.model.AccountAnalytics
import ru.plumsoftware.finance.domain.model.PeriodSummary
import ru.plumsoftware.finance.domain.model.SavingsIndex

interface AnalyticsRepository {
    suspend fun getPeriodSummary(startMillis: Long, endMillis: Long): PeriodSummary
    suspend fun getSavingsIndex(startMillis: Long, endMillis: Long): SavingsIndex
    suspend fun getAccountAnalytics(startMillis: Long, endMillis: Long): List<AccountAnalytics>
}
