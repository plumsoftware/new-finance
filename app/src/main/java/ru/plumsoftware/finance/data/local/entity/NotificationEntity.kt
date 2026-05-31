package ru.plumsoftware.finance.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.plumsoftware.finance.domain.model.NotificationSource
import ru.plumsoftware.finance.domain.model.NotificationType

@Entity(
    tableName = "notifications",
    indices = [
        Index("receivedAtMillis"),
        Index("isRead"),
        Index(value = ["messageId", "source"], unique = true),
    ],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val source: NotificationSource,
    val messageId: String?,
    val campaignId: String?,
    val dataJson: String,
    val receivedAtMillis: Long,
    val isRead: Boolean,
    val isDisplayed: Boolean,
    val notificationType: NotificationType = NotificationType.MONTHLY_SUMMARY,
    val titleRes: Int? = null,
    val bodyRes: Int? = null,
    val bodyArgsJson: String = "[]",
    val relatedCategoryId: Long? = null,
)
