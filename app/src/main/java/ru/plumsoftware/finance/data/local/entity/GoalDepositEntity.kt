package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_deposits",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("goalId"), Index("createdAtMillis"), Index("transactionId")],
)
data class GoalDepositEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val amountMinor: Long,
    val note: String?,
    val createdAtMillis: Long,
    val currencyCode: String = "RUB",
    val accountId: Long? = null,
    val transactionId: Long? = null,
)
