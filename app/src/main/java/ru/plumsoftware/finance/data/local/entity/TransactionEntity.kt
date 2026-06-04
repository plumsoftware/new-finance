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
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("smartAssetId"),
        Index("dateMillis"),
        Index("type"),
        Index("accountId"),
        Index("currencyCode"),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amountMinor: Long,
    val categoryId: Long?,
    val smartAssetId: Long?,
    val note: String?,
    val dateMillis: Long,
    val createdAtMillis: Long,
    val accountId: Long = 1L,
    val currencyCode: String = "RUB",
    val originalAmountMinor: Long = 0L,
    val originalCurrencyCode: String? = null,
    val exchangeRate: Double = 1.0,
)
