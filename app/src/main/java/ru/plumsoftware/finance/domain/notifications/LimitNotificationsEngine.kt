package ru.plumsoftware.finance.domain.notifications

import ru.plumsoftware.finance.domain.model.AppNotification
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.NotificationSource
import ru.plumsoftware.finance.domain.model.NotificationType
import ru.plumsoftware.finance.domain.repository.NotificationRepository
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class LimitNotificationsEngine(
    private val notificationRepository: NotificationRepository,
) {

    suspend fun evaluateAfterLimitSet(
        category: Category,
        spentMinor: Long,
        limitMinor: Long,
        warningTitleRes: Int,
        warningBodyRes: Int,
        exceededTitleRes: Int,
        exceededBodyRes: Int,
        formatOverspend: (Long) -> String,
    ) {
        if (limitMinor <= 0L) return
        val ratio = spentMinor.toDouble() / limitMinor.toDouble()
        val percent = (ratio * 100).roundToInt().coerceAtMost(999)

        when {
            ratio >= 1.0 -> {
                val overspendMinor = spentMinor - limitMinor
                notificationRepository.save(
                    AppNotification(
                        type = NotificationType.LIMIT_EXCEEDED,
                        titleRes = exceededTitleRes,
                        bodyRes = exceededBodyRes,
                        bodyArgs = listOf(category.name, formatOverspend(overspendMinor)),
                        source = NotificationSource.IN_APP,
                        relatedCategoryId = category.id,
                        isDisplayed = true,
                    ),
                )
            }
            ratio >= 0.8 -> {
                notificationRepository.save(
                    AppNotification(
                        type = NotificationType.LIMIT_WARNING,
                        titleRes = warningTitleRes,
                        bodyRes = warningBodyRes,
                        bodyArgs = listOf(category.name, percent.toString()),
                        source = NotificationSource.IN_APP,
                        relatedCategoryId = category.id,
                        isDisplayed = true,
                    ),
                )
            }
        }
    }
}
