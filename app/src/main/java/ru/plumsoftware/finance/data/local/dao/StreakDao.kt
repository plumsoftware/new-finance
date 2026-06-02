package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.StreakDataEntity

@Dao
interface StreakDao {
    @Query("SELECT * FROM streak_data WHERE id = 1")
    fun observe(): Flow<StreakDataEntity?>

    @Query("SELECT * FROM streak_data WHERE id = 1")
    suspend fun get(): StreakDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: StreakDataEntity)
}
