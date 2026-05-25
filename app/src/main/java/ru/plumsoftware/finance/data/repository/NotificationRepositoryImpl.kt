package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.plumsoftware.finance.data.local.dao.NotificationDao
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.mapper.toEntity
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.NotificationSource
import ru.plumsoftware.finance.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
) : NotificationRepository {

    override fun observeAll(): Flow<List<AppNotification>> =
        notificationDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeUnread(): Flow<List<AppNotification>> =
        notificationDao.observeUnread().map { rows -> rows.map { it.toDomain() } }

    override fun observeUnreadCount(): Flow<Int> = notificationDao.observeUnreadCount()

    override suspend fun getById(id: Long): AppNotification? =
        notificationDao.getById(id)?.toDomain()

    override suspend fun save(notification: AppNotification): Long {
        if (notification.messageId != null &&
            existsByMessageId(notification.messageId, notification.source)
        ) {
            return -1L
        }
        val insertedId = notificationDao.insert(notification.toEntity().copy(id = 0))
        return if (insertedId == -1L) -1L else insertedId
    }

    override suspend fun markAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun markAsDisplayed(id: Long) {
        notificationDao.markAsDisplayed(id)
    }

    override suspend fun delete(id: Long) {
        notificationDao.deleteById(id)
    }

    override suspend fun deleteAll() {
        notificationDao.deleteAll()
    }

    override suspend fun existsByMessageId(
        messageId: String,
        source: NotificationSource,
    ): Boolean = notificationDao.countByMessageId(messageId, source) > 0
}
