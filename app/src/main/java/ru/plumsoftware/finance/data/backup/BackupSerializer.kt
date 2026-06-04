package ru.plumsoftware.finance.data.backup

import org.json.JSONArray
import org.json.JSONObject
import ru.plumsoftware.finance.domain.model.BACKUP_FORMAT_VERSION
import ru.plumsoftware.finance.domain.model.BackupAssetDto
import ru.plumsoftware.finance.domain.model.BackupAssetUsageDto
import ru.plumsoftware.finance.domain.model.BackupAccountDto
import ru.plumsoftware.finance.domain.model.BackupCategoryDto
import ru.plumsoftware.finance.domain.model.BackupLimitDto
import ru.plumsoftware.finance.domain.model.BackupMeta
import ru.plumsoftware.finance.domain.model.BackupModel
import ru.plumsoftware.finance.domain.model.BackupGoalDto
import ru.plumsoftware.finance.domain.model.BackupAchievementUnlockDto
import ru.plumsoftware.finance.domain.model.BackupGoalDepositDto
import ru.plumsoftware.finance.domain.model.BackupRecordCounts
import ru.plumsoftware.finance.domain.model.BackupRecurringDto
import ru.plumsoftware.finance.domain.model.BackupTransactionDto

object BackupSerializer {

    fun toJson(backup: BackupModel): String {
        val root = JSONObject()
        root.put("meta", metaToJson(backup.meta))
        root.put("accounts", accountsToJson(backup.accounts))
        root.put("categories", categoriesToJson(backup.categories))
        root.put("transactions", transactionsToJson(backup.transactions))
        root.put("recurringTransactions", recurringToJson(backup.recurringTransactions))
        root.put("assets", assetsToJson(backup.assets))
        root.put("assetUsageHistory", assetUsageToJson(backup.assetUsageHistory))
        root.put("limits", limitsToJson(backup.limits))
        root.put("goals", goalsToJson(backup.goals))
        root.put("goalDeposits", goalDepositsToJson(backup.goalDeposits))
        root.put("achievements", achievementsToJson(backup.achievements))
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
                accounts = countsJson?.optInt("accounts") ?: 0,
                categories = countsJson?.optInt("categories") ?: 0,
                transactions = countsJson?.optInt("transactions") ?: 0,
                recurring = countsJson?.optInt("recurring") ?: 0,
                assets = countsJson?.optInt("assets") ?: 0,
                limits = countsJson?.optInt("limits") ?: 0,
                goals = countsJson?.optInt("goals") ?: 0,
                goalDeposits = countsJson?.optInt("goalDeposits") ?: 0,
                achievements = countsJson?.optInt("achievements") ?: 0,
            ),
            fileName = fileName,
        )
        if (meta.version > BACKUP_FORMAT_VERSION) {
            error("Unsupported backup version: ${meta.version}")
        }
        return BackupModel(
            meta = meta,
            accounts = parseAccounts(root.optJSONArray("accounts")),
            categories = parseCategories(root.optJSONArray("categories")),
            transactions = parseTransactions(root.optJSONArray("transactions")),
            recurringTransactions = parseRecurring(root.optJSONArray("recurringTransactions")),
            assets = parseAssets(root.optJSONArray("assets")),
            assetUsageHistory = parseAssetUsage(root.optJSONArray("assetUsageHistory")),
            limits = parseLimits(root.optJSONArray("limits")),
            goals = parseGoals(root.optJSONArray("goals")),
            goalDeposits = parseGoalDeposits(root.optJSONArray("goalDeposits")),
            achievements = parseAchievements(root.optJSONArray("achievements")),
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
                .put("accounts", meta.recordCounts.accounts)
                .put("categories", meta.recordCounts.categories)
                .put("transactions", meta.recordCounts.transactions)
                .put("recurring", meta.recordCounts.recurring)
                .put("assets", meta.recordCounts.assets)
                .put("limits", meta.recordCounts.limits)
                .put("goals", meta.recordCounts.goals)
                .put("goalDeposits", meta.recordCounts.goalDeposits)
                .put("achievements", meta.recordCounts.achievements),
        )

    private fun accountsToJson(items: List<BackupAccountDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("type", item.type)
                    .put("currencyCode", item.currencyCode)
                    .put("colorHex", item.colorHex)
                    .put("emoji", item.emoji)
                    .put("initialBalance", item.initialBalance)
                    .put("sortOrder", item.sortOrder)
                    .put("isDefault", item.isDefault)
                    .put("isArchived", item.isArchived)
                    .put("createdAtMillis", item.createdAtMillis),
            )
        }
        return array
    }

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
                    .put("smartAssetId", item.smartAssetId)
                    .put("accountId", item.accountId)
                    .put("accountName", item.accountName)
                    .put("currencyCode", item.currencyCode)
                    .put("originalAmount", item.originalAmount)
                    .put("originalCurrencyCode", item.originalCurrencyCode)
                    .put("exchangeRate", item.exchangeRate),
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

    private fun goalsToJson(items: List<BackupGoalDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("emoji", item.emoji)
                    .put("targetAmount", item.targetAmount)
                    .put("savedAmount", item.savedAmount)
                    .put("colorHex", item.colorHex)
                    .put("deadlineMillis", item.deadlineMillis)
                    .put("note", item.note)
                    .put("showOnHome", item.showOnHome)
                    .put("isCompleted", item.isCompleted)
                    .put("createdAtMillis", item.createdAtMillis)
                    .put("currencyCode", item.currencyCode)
                    .put("accountId", item.accountId),
            )
        }
        return array
    }

    private fun achievementsToJson(items: List<BackupAchievementUnlockDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("key", item.key)
                    .put("unlockedAtMillis", item.unlockedAtMillis),
            )
        }
        return array
    }

    private fun goalDepositsToJson(items: List<BackupGoalDepositDto>): JSONArray {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("goalId", item.goalId)
                    .put("amount", item.amount)
                    .put("note", item.note)
                    .put("createdAtMillis", item.createdAtMillis)
                    .put("currencyCode", item.currencyCode)
                    .put("accountId", item.accountId),
            )
        }
        return array
    }

    private fun parseAccounts(array: JSONArray?): List<BackupAccountDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupAccountDto(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        type = obj.optString("type", "DEBIT"),
                        currencyCode = obj.optString("currencyCode", "RUB"),
                        colorHex = obj.optString("colorHex", "#007AFF"),
                        emoji = obj.optString("emoji", "💳"),
                        initialBalance = obj.optDouble("initialBalance", 0.0),
                        sortOrder = obj.optInt("sortOrder", 0),
                        isDefault = obj.optBoolean("isDefault", false),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                    ),
                )
            }
        }
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
                        accountId = obj.optLong("accountId", 1L),
                        accountName = obj.optString("accountName").takeIf { it.isNotBlank() },
                        currencyCode = obj.optString("currencyCode", "RUB"),
                        originalAmount = obj.optDouble("originalAmount", obj.getDouble("amount")),
                        originalCurrencyCode = obj.optString("originalCurrencyCode")
                            .takeIf { it.isNotBlank() },
                        exchangeRate = obj.optDouble("exchangeRate", 1.0),
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

    private fun parseGoals(array: JSONArray?): List<BackupGoalDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupGoalDto(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        emoji = obj.optString("emoji", "🎯"),
                        targetAmount = obj.getDouble("targetAmount"),
                        savedAmount = obj.optDouble("savedAmount", 0.0),
                        colorHex = obj.optString("colorHex", "#007AFF"),
                        deadlineMillis = obj.optLong("deadlineMillis")
                            .takeIf { obj.has("deadlineMillis") && !obj.isNull("deadlineMillis") },
                        note = obj.optString("note").takeIf { it.isNotBlank() },
                        showOnHome = obj.optBoolean("showOnHome", false),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        createdAtMillis = obj.optLong(
                            "createdAtMillis",
                            System.currentTimeMillis(),
                        ),
                        currencyCode = obj.optString("currencyCode", "RUB"),
                        accountId = obj.optLong("accountId")
                            .takeIf { obj.has("accountId") && !obj.isNull("accountId") },
                    ),
                )
            }
        }
    }

    private fun parseAchievements(array: JSONArray?): List<BackupAchievementUnlockDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupAchievementUnlockDto(
                        key = obj.getString("key"),
                        unlockedAtMillis = obj.optLong(
                            "unlockedAtMillis",
                            System.currentTimeMillis(),
                        ),
                    ),
                )
            }
        }
    }

    private fun parseGoalDeposits(array: JSONArray?): List<BackupGoalDepositDto> {
        if (array == null) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    BackupGoalDepositDto(
                        id = obj.getLong("id"),
                        goalId = obj.getLong("goalId"),
                        amount = obj.getDouble("amount"),
                        note = obj.optString("note").takeIf { it.isNotBlank() },
                        createdAtMillis = obj.optLong(
                            "createdAtMillis",
                            System.currentTimeMillis(),
                        ),
                        currencyCode = obj.optString("currencyCode", "RUB"),
                        accountId = obj.optLong("accountId")
                            .takeIf { obj.has("accountId") && !obj.isNull("accountId") },
                    ),
                )
            }
        }
    }
}
