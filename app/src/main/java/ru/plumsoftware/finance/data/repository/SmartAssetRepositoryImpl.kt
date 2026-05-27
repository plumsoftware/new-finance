package ru.plumsoftware.finance.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.entity.TransactionEntity
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.data.util.SmartAssetLogic
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.isWeekday
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetUsage
import ru.plumsoftware.finance.domain.model.SmartAssetWithUsages
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository

class SmartAssetRepositoryImpl(
    private val database: FinanceDatabase,
    private val smartAssetDao: SmartAssetDao,
    private val transactionDao: TransactionDao,
) : SmartAssetRepository {

    override fun observeActive(): Flow<List<SmartAsset>> =
        smartAssetDao.observeActive().map { rows -> rows.map { it.toDomain() } }

    override fun observeByStatus(status: SmartAssetStatus): Flow<List<SmartAsset>> =
        smartAssetDao.observeByStatus(status).map { rows -> rows.map { it.toDomain() } }

    override fun observeTotalSavedAllTime(): Flow<Long> =
        smartAssetDao.observeTotalSavedAllTime()

    override suspend fun getById(id: Long): SmartAsset? =
        smartAssetDao.getById(id)?.toDomain()

    override suspend fun getWithUsages(id: Long): SmartAssetWithUsages? {
        val asset = smartAssetDao.getById(id)?.toDomain() ?: return null
        val usages = smartAssetDao.getUsages(id).map { it.toDomain() }
        return SmartAssetWithUsages(asset, usages)
    }

    override suspend fun create(
        asset: SmartAsset,
        categoryId: Long?,
        createPurchaseExpense: Boolean,
    ): Long {
        val assetId = smartAssetDao.insert(asset.toEntity().copy(id = 0))
        if (createPurchaseExpense) {
            val now = System.currentTimeMillis()
            transactionDao.insert(
                TransactionEntity(
                    type = TransactionType.EXPENSE,
                    amountMinor = asset.purchaseCostMinor,
                    categoryId = categoryId,
                    smartAssetId = assetId,
                    note = asset.note ?: asset.name,
                    dateMillis = asset.purchasedAtMillis,
                    createdAtMillis = now,
                )
            )
        }
        return assetId
    }

    override suspend fun update(asset: SmartAsset) {
        smartAssetDao.update(asset.toEntity())
    }

    override suspend fun deactivate(id: Long) {
        smartAssetDao.deactivate(id)
    }

    override suspend fun recordUsage(
        smartAssetId: Long,
        savedAmountMinor: Long?,
        note: String?,
        usedAtMillis: Long,
    ): Long = database.withTransaction {
        val current = smartAssetDao.getById(smartAssetId)?.toDomain()
            ?: error("Smart asset $smartAssetId not found")
        val saved = savedAmountMinor ?: current.alternativeCostMinor
        val usageId = smartAssetDao.insertUsage(
            SmartAssetUsage(
                smartAssetId = smartAssetId,
                savedAmountMinor = saved,
                usedAtMillis = usedAtMillis,
                note = note,
            ).toEntity(),
        )
        val updated = current.copy(
            totalSavedMinor = current.totalSavedMinor + saved,
            totalUses = current.totalUses + 1,
            status = SmartAssetLogic.resolveStatus(current, saved),
        )
        smartAssetDao.update(updated.toEntity())
        usageId
    }

    override suspend fun applyAutoSavingsForWeekdays(dayMillis: Long) {
        if (!isWeekday(dayMillis)) return
        val dayStart = startOfDayMillis(dayMillis)
        val dayEnd = endOfDayMillis(dayMillis)
        smartAssetDao.getAutoWeekdayAssets().forEach { entity ->
            if (smartAssetDao.countUsagesOnDay(entity.id, dayStart, dayEnd) > 0) return@forEach
            recordUsage(
                smartAssetId = entity.id,
                savedAmountMinor = entity.alternativeCostMinor,
                note = null,
                usedAtMillis = dayMillis,
            )
        }
    }

    override suspend fun countActive(): Int = smartAssetDao.countActive()
}
