package ru.plumsoftware.finance.domain.repository

import ru.plumsoftware.finance.domain.model.ExportData
import ru.plumsoftware.finance.domain.model.ExportOptions
import ru.plumsoftware.finance.domain.model.ExportPeriod

interface ExportRepository {
    suspend fun getExportData(
        period: ExportPeriod,
        include: ExportOptions,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null,
    ): ExportData
}
