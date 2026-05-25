package ru.plumsoftware.finance.domain.model

data class AppNotification(
    val id: Long = 0,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val source: NotificationSource,
    val messageId: String?,
    val campaignId: String?,
    val data: Map<String, String>,
    val receivedAtMillis: Long,
    val isRead: Boolean = false,
    val isDisplayed: Boolean = false,
)
