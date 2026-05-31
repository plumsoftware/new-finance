package ru.plumsoftware.finance.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.repository.NotificationRepository

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    val notifications: StateFlow<List<AppNotification>> = notificationRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> = notificationRepository.observeUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun markAllRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun markRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            notificationRepository.delete(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationRepository.deleteAll()
        }
    }
}
