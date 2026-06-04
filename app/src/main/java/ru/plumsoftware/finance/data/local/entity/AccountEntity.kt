package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.plumsoftware.finance.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val currencyCode: String,
    val colorHex: String = "#007AFF",
    val emoji: String = "💳",
    val initialBalanceMinor: Long = 0,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = 0,
)
