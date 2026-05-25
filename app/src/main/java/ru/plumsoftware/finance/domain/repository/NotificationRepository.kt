package ru.plumsoftware.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.NotificationSource

interface NotificationRepository {
    fun observeAll(): Flow<List<AppNotification>>
    fun observeUnread(): Flow<List<AppNotification>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun getById(id: Long): AppNotification?
    suspend fun save(notification: AppNotification): Long
    suspend fun markAsRead(id: Long)
    suspend fun markAllAsRead()
    suspend fun markAsDisplayed(id: Long)
    suspend fun delete(id: Long)
    suspend fun deleteAll()
    suspend fun existsByMessageId(messageId: String, source: NotificationSource): Boolean
}
