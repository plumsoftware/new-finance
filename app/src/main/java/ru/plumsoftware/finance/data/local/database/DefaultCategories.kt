package ru.plumsoftware.finance.data.local.database

import android.content.Context
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.domain.model.CategoryType

object DefaultCategories {
    fun expense(context: Context): List<CategoryEntity> = listOf(
        category(context.getString(R.string.default_category_products), CategoryType.EXPENSE, "🛒", 0),
        category(context.getString(R.string.default_category_cafe_restaurants), CategoryType.EXPENSE, "☕", 1),
        category(context.getString(R.string.default_category_transport), CategoryType.EXPENSE, "🚕", 2),
        category(context.getString(R.string.default_category_housing), CategoryType.EXPENSE, "🏠", 3),
        category(context.getString(R.string.default_category_subscriptions), CategoryType.EXPENSE, "📱", 4),
        category(context.getString(R.string.default_category_health), CategoryType.EXPENSE, "💊", 5),
        category(context.getString(R.string.default_category_entertainment), CategoryType.EXPENSE, "🎬", 6),
        category(context.getString(R.string.default_category_shopping), CategoryType.EXPENSE, "🛍️", 7),
    )

    fun income(context: Context): List<CategoryEntity> = listOf(
        category(context.getString(R.string.default_category_salary), CategoryType.INCOME, "💼", 0),
        category(context.getString(R.string.default_category_freelance), CategoryType.INCOME, "💻", 1),
        category(context.getString(R.string.default_category_gifts), CategoryType.INCOME, "🎁", 2),
        category(context.getString(R.string.default_category_cashback_interest), CategoryType.INCOME, "💳", 3),
    )

    fun all(context: Context): List<CategoryEntity> = expense(context) + income(context)

    private fun category(
        name: String,
        type: CategoryType,
        icon: String,
        sortOrder: Int,
    ): CategoryEntity = CategoryEntity(
        name = name,
        type = type,
        icon = icon,
        colorArgb = null,
        isHidden = false,
        isSystem = true,
        sortOrder = sortOrder,
    )
}
