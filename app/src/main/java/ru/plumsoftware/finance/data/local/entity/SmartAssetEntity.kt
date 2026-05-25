package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetTrackingMode

@Entity(tableName = "smart_assets")
data class SmartAssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val purchaseCostMinor: Long,
    val alternativeCostMinor: Long,
    val trackingMode: SmartAssetTrackingMode,
    val status: SmartAssetStatus,
    val totalSavedMinor: Long,
    val totalUses: Int,
    val purchasedAtMillis: Long,
    val isActive: Boolean,
    val note: String?,
    val createdAtMillis: Long,
)
