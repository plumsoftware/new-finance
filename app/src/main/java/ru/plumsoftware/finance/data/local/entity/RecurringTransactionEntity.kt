package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.plumsoftware.finance.domain.model.RecurringFrequency

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId"), Index("nextDateMillis")],
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amountMinor: Long,
    val categoryId: Long,
    val isIncome: Boolean,
    val frequency: RecurringFrequency,
    val dayOfMonth: Int? = null,
    val nextDateMillis: Long,
    val isActive: Boolean = true,
    val note: String? = null,
)
