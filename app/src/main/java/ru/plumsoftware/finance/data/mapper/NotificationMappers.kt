package ru.plumsoftware.finance.data.mapper

import org.json.JSONArray
import org.json.JSONObject
import ru.plumsoftware.finance.data.local.entity.NotificationEntity
import ru.plumsoftware.finance.domain.model.AppNotification

fun NotificationEntity.toDomain(): AppNotification = AppNotification(
    id = id,
    type = notificationType,
    titleRes = titleRes,
    bodyRes = bodyRes,
    bodyArgs = bodyArgsJson.toArgsList(),
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
    relatedCategoryId = relatedCategoryId,
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
    notificationType = type,
    titleRes = titleRes,
    bodyRes = bodyRes,
    bodyArgsJson = bodyArgs.toArgsJson(),
    relatedCategoryId = relatedCategoryId,
)

fun List<String>.toArgsJson(): String {
    if (isEmpty()) return "[]"
    return JSONArray(this).toString()
}

fun String.toArgsList(): List<String> {
    if (isBlank() || this == "[]") return emptyList()
    return try {
        val array = JSONArray(this)
        buildList {
            for (index in 0 until array.length()) {
                add(array.optString(index))
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

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
