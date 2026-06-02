package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetUsage
import ru.plumsoftware.finance.domain.model.SmartAssetWithUsages

interface SmartAssetRepository {
    fun observeActive(): Flow<List<SmartAsset>>
    fun observeByStatus(status: SmartAssetStatus): Flow<List<SmartAsset>>
    fun observeTotalSavedAllTime(): Flow<Long>
    suspend fun getById(id: Long): SmartAsset?
    suspend fun getWithUsages(id: Long): SmartAssetWithUsages?
    suspend fun create(
        asset: SmartAsset,
        categoryId: Long?,
        createPurchaseExpense: Boolean = true,
    ): Long
    suspend fun update(asset: SmartAsset)
    suspend fun deactivate(id: Long)
    suspend fun recordUsage(
        smartAssetId: Long,
        savedAmountMinor: Long? = null,
        note: String? = null,
        usedAtMillis: Long = System.currentTimeMillis(),
    ): Long
    suspend fun applyAutoSavingsForWeekdays(dayMillis: Long = System.currentTimeMillis())
    suspend fun countActive(): Int
    suspend fun deleteAssetWithUsages(id: Long)
}
