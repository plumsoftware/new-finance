package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.plumsoftware.finance.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = SmartAssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["smartAssetId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("accountId"),
        Index("categoryId"),
        Index("smartAssetId"),
        Index("dateMillis"),
        Index("type"),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amountMinor: Long,
    val accountId: Long,
    val categoryId: Long?,
    val smartAssetId: Long?,
    val note: String?,
    val dateMillis: Long,
    val createdAtMillis: Long,
)
