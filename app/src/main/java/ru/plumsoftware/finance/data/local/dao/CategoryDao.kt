package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.domain.model.CategoryType

@Dao
interface CategoryDao {
    @Query(
        """
        SELECT * FROM categories
        WHERE type = :type
        AND (:includeHidden = 1 OR isHidden = 0)
        ORDER BY sortOrder ASC, id ASC
        """,
    )
    fun observeByType(type: CategoryType, includeHidden: Boolean): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("UPDATE categories SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: Long, hidden: Boolean)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)
}
