package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.data.mapper.toSpending
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryBudgetSpending
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.MonthPeriod
import ru.plumsoftware.finance.domain.repository.CategoryRepository

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) : CategoryRepository {

    override fun observeByType(type: CategoryType, includeHidden: Boolean): Flow<List<Category>> =
        categoryDao.observeByType(type, includeHidden).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun upsert(category: Category): Long {
        val entity = category.toEntity()
        return if (entity.id == 0L) categoryDao.insert(entity) else {
            categoryDao.update(entity)
            entity.id
        }
    }

    override suspend fun setHidden(id: Long, hidden: Boolean) {
        categoryDao.setHidden(id, hidden)
    }

    override suspend fun delete(id: Long) {
        val entity = categoryDao.getById(id) ?: return
        if (entity.isSystem) return
        categoryDao.delete(entity)
    }

    override suspend fun getTopSpendingByCategory(
        startMillis: Long,
        endMillis: Long,
        limit: Int,
    ): List<CategorySpending> {
        val rows = transactionDao.getTopExpenseByCategory(startMillis, endMillis, limit)
        val total = rows.sumOf { it.amountMinor }
        return rows.mapNotNull { row ->
            categoryDao.getById(row.categoryId)?.toDomain()?.toSpending(row.amountMinor, total)
        }
    }

    override fun getCategoryWithSpending(month: MonthPeriod): Flow<List<CategoryBudgetSpending>> {
        val (startMillis, endExclusiveMillis) = month.toMillisRange()
        return combine(
            categoryDao.observeByType(CategoryType.EXPENSE, includeHidden = false),
            transactionDao.observeExpenseByCategoryForPeriod(startMillis, endExclusiveMillis),
        ) { categories, spendingRows ->
            val spentByCategory = spendingRows.associate { it.categoryId to it.amountMinor }
            categories.map { entity ->
                val category = entity.toDomain()
                val spent = spentByCategory[category.id] ?: 0L
                val limit = category.monthlyLimitMinor
                val percentage = if (limit != null && limit > 0L) {
                    (spent.toFloat() / limit.toFloat()).coerceAtMost(1f)
                } else {
                    0f
                }
                CategoryBudgetSpending(
                    category = category,
                    spentMinor = spent,
                    limitMinor = limit,
                    percentage = percentage,
                )
            }.sortedByDescending { it.spentMinor }
        }
    }

    override suspend fun setLimit(id: Long, limitMinor: Long?) {
        categoryDao.setLimit(id, limitMinor)
    }
}
