package ru.plumsoftware.finance.data.local.database

import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.domain.model.CategoryType

object DefaultCategories {
    fun expense(): List<CategoryEntity> = listOf(
        category("Продукты", CategoryType.EXPENSE, "🛒", 0),
        category("Кафе и рестораны", CategoryType.EXPENSE, "☕", 1),
        category("Транспорт", CategoryType.EXPENSE, "🚕", 2),
        category("Жильё", CategoryType.EXPENSE, "🏠", 3),
        category("Подписки", CategoryType.EXPENSE, "📱", 4),
        category("Здоровье", CategoryType.EXPENSE, "💊", 5),
        category("Развлечения", CategoryType.EXPENSE, "🎬", 6),
        category("Шоппинг", CategoryType.EXPENSE, "🛍️", 7),
    )

    fun income(): List<CategoryEntity> = listOf(
        category("Зарплата", CategoryType.INCOME, "💼", 0),
        category("Фриланс", CategoryType.INCOME, "💻", 1),
        category("Подарки", CategoryType.INCOME, "🎁", 2),
        category("Кэшбек и проценты", CategoryType.INCOME, "💳", 3),
    )

    fun all(): List<CategoryEntity> = expense() + income()

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
