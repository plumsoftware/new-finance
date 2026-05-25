package ru.plumsoftware.finance.domain.repository

import ru.plumsoftware.finance.domain.model.PeriodSummary
import ru.plumsoftware.finance.domain.model.SavingsIndex

interface AnalyticsRepository {
    suspend fun getPeriodSummary(startMillis: Long, endMillis: Long): PeriodSummary
    suspend fun getSavingsIndex(startMillis: Long, endMillis: Long): SavingsIndex
}
