package ru.plumsoftware.finance.domain.model

data class AppNotification(
    val id: Long = 0,
    val type: NotificationType = NotificationType.MONTHLY_SUMMARY,
    val titleRes: Int? = null,
    val bodyRes: Int? = null,
    val bodyArgs: List<String> = emptyList(),
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val source: NotificationSource = NotificationSource.IN_APP,
    val messageId: String? = null,
    val campaignId: String? = null,
    val data: Map<String, String> = emptyMap(),
    val receivedAtMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDisplayed: Boolean = false,
    val relatedCategoryId: Long? = null,
)
