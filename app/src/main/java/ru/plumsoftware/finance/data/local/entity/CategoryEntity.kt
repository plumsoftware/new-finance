package ru.plumsoftware.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.plumsoftware.finance.domain.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val icon: String,
    val colorArgb: Long?,
    val isHidden: Boolean,
    val isSystem: Boolean,
    val sortOrder: Int,
    @ColumnInfo(name = "monthly_limit")
    val monthlyLimitMinor: Long? = null,
)
