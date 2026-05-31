package ru.plumsoftware.finance.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val type: CategoryType,
    val icon: String,
    val colorArgb: Long?,
    val isHidden: Boolean = false,
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val monthlyLimitMinor: Long? = null,
)
