package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.AchievementUnlockEntity

@Dao
interface AchievementUnlockDao {
    @Query("SELECT * FROM achievement_unlocks ORDER BY unlockedAtMillis DESC")
    fun observeAll(): Flow<List<AchievementUnlockEntity>>

    @Query("SELECT * FROM achievement_unlocks ORDER BY unlockedAtMillis DESC")
    suspend fun getAll(): List<AchievementUnlockEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: AchievementUnlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<AchievementUnlockEntity>)

    @Query("DELETE FROM achievement_unlocks")
    suspend fun deleteAll()
}
