package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryBudgetSpending
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.MonthPeriod

interface CategoryRepository {
    fun observeByType(type: CategoryType, includeHidden: Boolean = false): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun upsert(category: Category): Long
    suspend fun setHidden(id: Long, hidden: Boolean)
    suspend fun delete(id: Long)
    suspend fun getTopSpendingByCategory(
        startMillis: Long,
        endMillis: Long,
        limit: Int = 10,
    ): List<CategorySpending>
    fun getCategoryWithSpending(month: MonthPeriod = MonthPeriod.current()): Flow<List<CategoryBudgetSpending>>
    suspend fun setLimit(id: Long, limitMinor: Long?)
}
