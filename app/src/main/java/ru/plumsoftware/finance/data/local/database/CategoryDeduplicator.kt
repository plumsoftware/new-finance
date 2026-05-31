package ru.plumsoftware.finance.data.local.database

import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import java.util.Locale

internal object CategoryDeduplicator {

    fun categoryKey(entity: CategoryEntity): Pair<ru.plumsoftware.finance.domain.model.CategoryType, String> =
        entity.type to entity.name.trim().lowercase(Locale.ROOT)

    suspend fun deduplicate(categoryDao: CategoryDao, transactionDao: TransactionDao) {
        val all = categoryDao.getAllSync()
        all.groupBy { categoryKey(it) }
            .filter { it.value.size > 1 }
            .forEach { (_, duplicates) ->
                val keeper = duplicates.sortedWith(
                    compareByDescending<CategoryEntity> { it.isSystem }
                        .thenBy { it.sortOrder }
                        .thenBy { it.id },
                ).first()
                duplicates
                    .filter { it.id != keeper.id }
                    .forEach { duplicate ->
                        transactionDao.reassignCategory(duplicate.id, keeper.id)
                        categoryDao.delete(duplicate)
                    }
            }
    }
}
