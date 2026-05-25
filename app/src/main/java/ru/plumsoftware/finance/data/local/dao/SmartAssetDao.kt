package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.SmartAssetEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetUsageEntity
import ru.plumsoftware.finance.domain.model.SmartAssetStatus

@Dao
interface SmartAssetDao {
    @Query(
        """
        SELECT * FROM smart_assets
        WHERE isActive = 1
        ORDER BY purchasedAtMillis DESC
        """,
    )
    fun observeActive(): Flow<List<SmartAssetEntity>>

    @Query(
        """
        SELECT * FROM smart_assets
        WHERE isActive = 1 AND status = :status
        ORDER BY purchasedAtMillis DESC
        """,
    )
    fun observeByStatus(status: SmartAssetStatus): Flow<List<SmartAssetEntity>>

    @Query("SELECT COALESCE(SUM(totalSavedMinor), 0) FROM smart_assets")
    fun observeTotalSavedAllTime(): Flow<Long>

    @Query("SELECT * FROM smart_assets WHERE id = :id")
    suspend fun getById(id: Long): SmartAssetEntity?

    @Query("SELECT COUNT(*) FROM smart_assets WHERE isActive = 1")
    suspend fun countActive(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: SmartAssetEntity): Long

    @Update
    suspend fun update(asset: SmartAssetEntity)

    @Query("UPDATE smart_assets SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: SmartAssetUsageEntity): Long

    @Query(
        """
        SELECT * FROM smart_asset_usages
        WHERE smartAssetId = :smartAssetId
        ORDER BY usedAtMillis DESC
        """,
    )
    suspend fun getUsages(smartAssetId: Long): List<SmartAssetUsageEntity>

    @Query(
        """
        SELECT * FROM smart_assets
        WHERE isActive = 1 AND trackingMode = 'AUTO_WEEKDAYS'
        """,
    )
    suspend fun getAutoWeekdayAssets(): List<SmartAssetEntity>

    @Query(
        """
        SELECT COUNT(*) FROM smart_asset_usages
        WHERE smartAssetId = :smartAssetId
        AND usedAtMillis >= :dayStartMillis AND usedAtMillis < :dayEndMillis
        """,
    )
    suspend fun countUsagesOnDay(smartAssetId: Long, dayStartMillis: Long, dayEndMillis: Long): Int
}
