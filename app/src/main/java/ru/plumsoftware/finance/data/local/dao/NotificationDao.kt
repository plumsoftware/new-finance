package ru.plumsoftware.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.data.local.entity.NotificationEntity
import ru.plumsoftware.finance.domain.model.NotificationSource

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY receivedAtMillis DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT * FROM notifications
        WHERE isRead = 0
        ORDER BY receivedAtMillis DESC
        """,
    )
    fun observeUnread(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getById(id: Long): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE notifications SET isDisplayed = 1 WHERE id = :id")
    suspend fun markAsDisplayed(id: Long)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query(
        """
        SELECT COUNT(*) FROM notifications
        WHERE messageId = :messageId AND source = :source
        """,
    )
    suspend fun countByMessageId(messageId: String, source: NotificationSource): Int
}
