package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "smart_asset_usages",
    foreignKeys = [
        ForeignKey(
            entity = SmartAssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["smartAssetId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("smartAssetId"), Index("usedAtMillis")],
)
data class SmartAssetUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smartAssetId: Long,
    val savedAmountMinor: Long,
    val usedAtMillis: Long,
    val note: String?,
)
