package ru.plumsoftware.finance.data.mapper

import org.json.JSONObject
import ru.plumsoftware.finance.data.local.entity.NotificationEntity
import ru.plumsoftware.finance.domain.model.AppNotification

fun NotificationEntity.toDomain(): AppNotification = AppNotification(
    id = id,
    title = title,
    body = body,
    imageUrl = imageUrl,
    source = source,
    messageId = messageId,
    campaignId = campaignId,
    data = dataJson.toStringMap(),
    receivedAtMillis = receivedAtMillis,
    isRead = isRead,
    isDisplayed = isDisplayed,
)

fun AppNotification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    title = title,
    body = body,
    imageUrl = imageUrl,
    source = source,
    messageId = messageId,
    campaignId = campaignId,
    dataJson = data.toJsonString(),
    receivedAtMillis = receivedAtMillis,
    isRead = isRead,
    isDisplayed = isDisplayed,
)

fun Map<String, String>.toJsonString(): String {
    if (isEmpty()) return "{}"
    val json = JSONObject()
    forEach { (key, value) -> json.put(key, value) }
    return json.toString()
}

fun String.toStringMap(): Map<String, String> {
    if (isBlank() || this == "{}") return emptyMap()
    return try {
        val json = JSONObject(this)
        buildMap {
            json.keys().forEach { key ->
                put(key, json.optString(key))
            }
        }
    } catch (_: Exception) {
        emptyMap()
    }
}
