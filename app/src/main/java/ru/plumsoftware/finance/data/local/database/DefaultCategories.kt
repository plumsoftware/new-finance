package ru.plumsoftware.finance.data.local.database

import android.content.Context
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.domain.model.CategoryType

/** Категории по умолчанию; цвета — по таблице ТЗ §3.1 (иконка на подложке цвета 13%). */
object DefaultCategories {
    fun expense(context: Context): List<CategoryEntity> = listOf(
        category(context.getString(R.string.default_category_products), CategoryType.EXPENSE, "🛒", 0, 0xFF34C759L),
        category(context.getString(R.string.default_category_cafe_restaurants), CategoryType.EXPENSE, "☕", 1, 0xFFFF9500L),
        category(context.getString(R.string.default_category_transport), CategoryType.EXPENSE, "🚕", 2, 0xFF007AFFL),
        category(context.getString(R.string.default_category_housing), CategoryType.EXPENSE, "🏠", 3, 0xFF5856D6L),
        category(context.getString(R.string.default_category_subscriptions), CategoryType.EXPENSE, "📺", 4, 0xFFAF52DEL),
        category(context.getString(R.string.default_category_health), CategoryType.EXPENSE, "💊", 5, 0xFFFF3B30L),
        category(context.getString(R.string.default_category_entertainment), CategoryType.EXPENSE, "🎬", 6, 0xFFFF2D55L),
        category(context.getString(R.string.default_category_shopping), CategoryType.EXPENSE, "🛍️", 7, 0xFF5AC8FAL),
    )

    fun income(context: Context): List<CategoryEntity> = listOf(
        category(context.getString(R.string.default_category_salary), CategoryType.INCOME, "💼", 0, 0xFF34C759L),
        category(context.getString(R.string.default_category_freelance), CategoryType.INCOME, "💻", 1, 0xFF007AFFL),
        category(context.getString(R.string.default_category_gifts), CategoryType.INCOME, "🎁", 2, 0xFFFF9500L),
        category(context.getString(R.string.default_category_cashback_interest), CategoryType.INCOME, "💳", 3, 0xFF5856D6L),
    )

    fun all(context: Context): List<CategoryEntity> = expense(context) + income(context)

    private fun category(
        name: String,
        type: CategoryType,
        icon: String,
        sortOrder: Int,
        colorArgb: Long,
    ): CategoryEntity = CategoryEntity(
        name = name,
        type = type,
        icon = icon,
        colorArgb = colorArgb,
        isHidden = false,
        isSystem = true,
        sortOrder = sortOrder,
    )
}
