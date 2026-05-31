package ru.plumsoftware.finance.data.backup

import org.json.JSONArray
import org.json.JSONObject
import ru.plumsoftware.finance.domain.model.BACKUP_FORMAT_VERSION
import ru.plumsoftware.finance.domain.model.BackupAssetDto
import ru.plumsoftware.finance.domain.model.BackupAssetUsageDto
import ru.plumsoftware.finance.domain.model.BackupCategoryDto
import ru.plumsoftware.finance.domain.model.BackupLimitDto
import ru.plumsoftware.finance.domain.model.BackupMeta
import ru.plumsoftware.finance.domain.model.BackupModel
import ru.plumsoftware.finance.domain.model.BackupRecordCounts
import ru.plumsoftware.finance.domain.model.BackupRecurringDto
import ru.plumsoftware.finance.domain.model.BackupTransactionDto

object BackupSerializer {

    fun toJson(backup: BackupModel): String {
        val root = JSONObject()
        root.put("meta", metaToJson(backup.meta))
        root.put("categories", categoriesToJson(backup.categories))
        root.put("transactions", transactionsToJson(backup.transactions))
        root.put("recurringTransactions", recurringToJson(backup.recurringTransactions))
        root.put("assets", assetsToJson(backup.assets))
        root.put("assetUsageHistory", assetUsageToJson(backup.assetUsageHistory))
        root.put("limits", limitsToJson(backup.limits))
        return root.toString(2)
    }

    fun fromJson(json: String, fileName: String = ""): BackupModel {
        val root = JSONObject(json)
        val metaJson = root.getJSONObject("meta")
        val countsJson = metaJson.optJSONObject("recordCounts")
        val meta = BackupMeta(
            version = metaJson.optInt("version", 1),
            appVersion = metaJson.optString("appVersion", ""),
            exportedAt = metaJson.optString("exportedAt", ""),
            deviceModel = metaJson.optString("deviceModel", ""),
            recordCounts = BackupRecordCounts(
                categories = countsJson?.optInt("categories") ?: 0,
                transactions = countsJson?.optInt("transactions") ?: 0,
                recurring = countsJson?.optInt("recurring") ?: 0,
                assets = countsJson?.optInt("assets") ?: 0,
                limits = countsJson?.optInt("limits") ?: 0,
            ),
            fileName = fileName,
        )
        if (meta.version > BACKUP_FORMAT_VERSION) {
            error("Unsupported backup version: ${meta.version}")
        }
        return BackupModel(
            meta = meta,
            categories = parseCategories(root.optJSONArray("categories")),
            transactions = parseTransactions(root.optJSONArray("transactions")),
            recurringTransactions = parseRecurring(root.optJSONArray("recurringTransactions")),
            assets = parseAssets(root.optJSONArray("assets")),
            assetUsageHistory = parseAssetUsage(root.optJSONArray("assetUsageHistory")),
            limits = parseLimits(root.optJSONArray("limits")),
        )
    }

    private fun metaToJson(meta: BackupMeta): JSONObject = JSONObject()
        .put("version", meta.version)
        .put("appVersion", meta.appVersion)
        .put("exportedAt", meta.exportedAt)
        .put("deviceModel", meta.deviceModel)
        .put(
            "recordCounts",
            JSONObject()
                .put("categories", meta.recordCounts.categories)
                .put("transactions", meta.recordCounts.transactions)
                .put("recurring", meta.recordCounts.recurring)
                .put("assets", meta.recordCounts.assets)
                .put("limits", meta.recordCounts.limits),
        )

    private fun categoriesToJson(items: List<BackupCategoryDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("emoji", item.emoji)
                    .put("colorHex", item.colorHex)
                    .put("isIncome", item.isIncome)
                    .put("sortOrder", item.sortOrder),
            )
        }
        return array
    }

    private fun transactionsToJson(items: List<BackupTransactionDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("amount", item.amount)
                    .put("categoryId", item.categoryId)
                    .put("isIncome", item.isIncome)
                    .put("date", item.date)
                    .put("note", item.note)
                    .put("smartAssetId", item.smartAssetId),
            )
        }
        return array
    }

    private fun recurringToJson(items: List<BackupRecurringDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("amount", item.amount)
                    .put("categoryId", item.categoryId)
                    .put("isIncome", item.isIncome)
                    .put("frequency", item.frequency)
                    .put("dayOfMonth", item.dayOfMonth)
                    .put("nextDate", item.nextDate)
                    .put("isActive", item.isActive)
                    .put("note", item.note),
            )
        }
        return array
    }

    private fun assetsToJson(items: List<BackupAssetDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("emoji", item.emoji)
                    .put("purchaseCost", item.purchaseCost)
                    .put("savingsPerUse", item.savingsPerUse)
                    .put("totalSaved", item.totalSaved)
                    .put("usageCount", item.usageCount)
                    .put("createdAt", item.createdAt)
                    .put("isActive", item.isActive)
                    .put("note", item.note)
                    .put("trackingMode", item.trackingMode)
                    .put("status", item.status),
            )
        }
        return array
    }

    private fun assetUsageToJson(items: List<BackupAssetUsageDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("assetId", item.assetId)
                    .put("date", item.date)
                    .put("savedAmount", item.savedAmount)
                    .put("note", item.note),
            )
        }
        return array
    }

    private fun limitsToJson(items: List<BackupLimitDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("categoryId", item.categoryId)
                    .put("monthlyLimit", item.monthlyLimit),
            )
        }
        return array
    }

    private fun parseCategories(array: JSONArray?): List<BackupCategoryDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupCategoryDto(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        emoji = obj.optString("emoji", obj.optString("icon", "📁")),
                        colorHex = obj.optString("colorHex").takeIf { it.isNotBlank() },
                        isIncome = obj.optBoolean("isIncome", false),
                        sortOrder = obj.optInt("sortOrder", 0),
                    ),
                )
            }
        }
    }

    private fun parseTransactions(array: JSONArray?): List<BackupTransactionDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupTransactionDto(
                        id = obj.getLong("id"),
                        amount = obj.getDouble("amount"),
                        categoryId = obj.optLong("categoryId").takeIf { obj.has("categoryId") && !obj.isNull("categoryId") },
                        isIncome = obj.optBoolean("isIncome", false),
                        date = obj.getString("date"),
                        note = obj.optString("note").takeIf { it.isNotBlank() },
                        smartAssetId = obj.optLong("smartAssetId").takeIf { obj.has("smartAssetId") && !obj.isNull("smartAssetId") },
                    ),
                )
            }
        }
    }

    private fun parseRecurring(array: JSONArray?): List<BackupRecurringDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupRecurringDto(
                        id = obj.getLong("id"),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        categoryId = obj.getLong("categoryId"),
                        isIncome = obj.optBoolean("isIncome", false),
                        frequency = obj.getString("frequency"),
                        dayOfMonth = obj.optInt("dayOfMonth").takeIf { obj.has("dayOfMonth") && !obj.isNull("dayOfMonth") },
                        nextDate = obj.getString("nextDate"),
                        isActive = obj.optBoolean("isActive", true),
                        note = obj.optString("note").takeIf { it.isNotBlank() },
                    ),
                )
            }
        }
    }

    private fun parseAssets(array: JSONArray?): List<BackupAssetDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupAssetDto(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        emoji = obj.optString("emoji", obj.optString("icon", "♻️")),
                        purchaseCost = obj.getDouble("purchaseCost"),
                        savingsPerUse = obj.getDouble("savingsPerUse"),
                        totalSaved = obj.getDouble("totalSaved"),
                        usageCount = obj.optInt("usageCount", 0),
                        createdAt = obj.getString("createdAt"),
                        isActive = obj.optBoolean("isActive", true),
                        note = obj.optString("note").takeIf { it.isNotBlank() },
                        trackingMode = obj.optString("trackingMode", "MANUAL"),
                        status = obj.optString("status", "PAYING_OFF"),
                    ),
                )
            }
        }
    }

    private fun parseAssetUsage(array: JSONArray?): List<BackupAssetUsageDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupAssetUsageDto(
                        assetId = obj.getLong("assetId"),
                        date = obj.getString("date"),
                        savedAmount = obj.getDouble("savedAmount"),
                        note = obj.optString("note").takeIf { it.isNotBlank() },
                    ),
                )
            }
        }
    }

    private fun parseLimits(array: JSONArray?): List<BackupLimitDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupLimitDto(
                        categoryId = obj.getLong("categoryId"),
                        monthlyLimit = obj.getDouble("monthlyLimit"),
                    ),
                )
            }
        }
    }
}
