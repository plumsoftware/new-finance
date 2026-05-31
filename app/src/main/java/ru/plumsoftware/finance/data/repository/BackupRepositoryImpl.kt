package ru.plumsoftware.finance.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.room.withTransaction
import ru.plumsoftware.finance.BuildConfig
import ru.plumsoftware.finance.data.backup.BackupFileWriter
import ru.plumsoftware.finance.data.backup.BackupSerializer
import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.RecurringTransactionDao
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.data.local.entity.RecurringTransactionEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetUsageEntity
import ru.plumsoftware.finance.data.local.entity.TransactionEntity
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
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
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.ExportFormat
import ru.plumsoftware.finance.domain.model.ExportPeriod
import ru.plumsoftware.finance.domain.model.ImportResult
import ru.plumsoftware.finance.domain.model.ImportStrategy
import ru.plumsoftware.finance.domain.model.RecurringFrequency
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetTrackingMode
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.BackupRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class BackupRepositoryImpl(
    private val context: Context,
    private val database: FinanceDatabase,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val recurringDao: RecurringTransactionDao,
    private val smartAssetDao: SmartAssetDao,
) : BackupRepository {

    override suspend fun buildBackup(
        period: ExportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
    ): BackupModel {
        val (startMillis, endMillis) = resolvePeriodMillis(period, customStartMillis, customEndMillis)
        val categories = categoryDao.getAllSync().map { it.toDomain() }
        val transactions = if (period == ExportPeriod.ALL_TIME) {
            transactionDao.getAllSync()
        } else {
            transactionDao.getByPeriodSync(startMillis, endMillis)
        }.map { it.toDomain() }
        val recurring = recurringDao.getAllSync().map { it.toDomain() }
        val assets = smartAssetDao.getAllSync().map { it.toDomain() }
        val usages = smartAssetDao.getAllUsagesSync().map { it.toDomain() }
        val limits = categories.mapNotNull { category ->
            category.monthlyLimitMinor?.let { limit ->
                BackupLimitDto(
                    categoryId = category.id,
                    monthlyLimit = minorToMajor(limit),
                )
            }
        }

        val backupCategories = categories.map { category ->
            BackupCategoryDto(
                id = category.id,
                name = category.name,
                emoji = category.icon,
                colorHex = category.colorArgb?.let { colorArgbToHex(it) },
                isIncome = category.type == CategoryType.INCOME,
                sortOrder = category.sortOrder,
            )
        }
        val backupTransactions = transactions.map { tx ->
            BackupTransactionDto(
                id = tx.id,
                amount = minorToMajor(tx.amountMinor),
                categoryId = tx.categoryId,
                isIncome = tx.type == TransactionType.INCOME,
                date = formatDate(tx.dateMillis),
                note = tx.note,
                smartAssetId = tx.smartAssetId,
            )
        }
        val backupRecurring = recurring.map { item ->
            BackupRecurringDto(
                id = item.id,
                title = item.title,
                amount = minorToMajor(item.amountMinor),
                categoryId = item.categoryId,
                isIncome = item.isIncome,
                frequency = item.frequency.name,
                dayOfMonth = item.dayOfMonth,
                nextDate = formatDate(item.nextDateMillis),
                isActive = item.isActive,
                note = item.note,
            )
        }
        val backupAssets = assets.map { asset ->
            BackupAssetDto(
                id = asset.id,
                name = asset.name,
                emoji = asset.icon,
                purchaseCost = minorToMajor(asset.purchaseCostMinor),
                savingsPerUse = minorToMajor(asset.alternativeCostMinor),
                totalSaved = minorToMajor(asset.totalSavedMinor),
                usageCount = asset.totalUses,
                createdAt = formatDate(asset.createdAtMillis),
                isActive = asset.isActive,
                note = asset.note,
                trackingMode = asset.trackingMode.name,
                status = asset.status.name,
            )
        }
        val backupUsages = usages.map { usage ->
            BackupAssetUsageDto(
                assetId = usage.smartAssetId,
                date = formatDate(usage.usedAtMillis),
                savedAmount = minorToMajor(usage.savedAmountMinor),
                note = usage.note,
            )
        }

        val meta = BackupMeta(
            version = BACKUP_FORMAT_VERSION,
            appVersion = BuildConfig.VERSION_NAME,
            exportedAt = Instant.now().toString(),
            deviceModel = Build.MODEL.orEmpty(),
            recordCounts = BackupRecordCounts(
                categories = backupCategories.size,
                transactions = backupTransactions.size,
                recurring = backupRecurring.size,
                assets = backupAssets.size,
                limits = limits.size,
            ),
        )

        return BackupModel(
            meta = meta,
            categories = backupCategories,
            transactions = backupTransactions,
            recurringTransactions = backupRecurring,
            assets = backupAssets,
            assetUsageHistory = backupUsages,
            limits = limits,
        )
    }

    override suspend fun exportToUri(backup: BackupModel, format: ExportFormat): Uri {
        val json = BackupSerializer.toJson(backup)
        return BackupFileWriter.write(context, json, format)
    }

    override suspend fun parseFromUri(uri: Uri, fileName: String): BackupModel {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: error("Cannot read backup file")
        return BackupSerializer.fromJson(json, fileName)
    }

    override fun parseFromJson(json: String, fileName: String): BackupModel =
        BackupSerializer.fromJson(json, fileName)

    override suspend fun importBackup(
        backup: BackupModel,
        strategy: ImportStrategy,
        onProgress: (Float) -> Unit,
    ): ImportResult {
        var added = 0
        var updated = 0
        var skipped = 0

        database.withTransaction {
            if (strategy == ImportStrategy.OVERWRITE) {
                onProgress(0.05f)
                transactionDao.deleteAll()
                recurringDao.deleteAll()
                smartAssetDao.deleteAllUsages()
                smartAssetDao.deleteAllAssets()
                categoryDao.deleteAll()
            }

            val existingCategories = categoryDao.getAllSync()
            val categoryIdMap = mutableMapOf<Long, Long>()
            val totalSteps = 5f
            var step = 0f

            backup.categories.forEach { dto ->
                val type = if (dto.isIncome) CategoryType.INCOME else CategoryType.EXPENSE
                val existing = existingCategories.find {
                    it.name.equals(dto.name, ignoreCase = true) && it.type == type
                }
                val entity = categoryEntityFromDto(dto, type)
                when {
                    strategy == ImportStrategy.OVERWRITE -> {
                        categoryDao.insertAllReplace(listOf(entity.copy(id = dto.id)))
                        categoryIdMap[dto.id] = dto.id
                        added++
                    }
                    existing == null -> {
                        val newId = categoryDao.insert(entity)
                        categoryIdMap[dto.id] = newId
                        added++
                    }
                    strategy == ImportStrategy.MERGE -> {
                        categoryIdMap[dto.id] = existing.id
                        skipped++
                    }
                    else -> {
                        categoryDao.insertAllReplace(
                            listOf(
                                entity.copy(
                                    id = existing.id,
                                    isSystem = existing.isSystem,
                                ),
                            ),
                        )
                        categoryIdMap[dto.id] = existing.id
                        updated++
                    }
                }
            }
            step++
            onProgress(step / totalSteps)

            backup.limits.forEach { limit ->
                val localCategoryId = categoryIdMap[limit.categoryId] ?: return@forEach
                categoryDao.setLimit(localCategoryId, majorToMinor(limit.monthlyLimit))
            }

            val existingTransactions = if (strategy == ImportStrategy.OVERWRITE) {
                emptyList()
            } else {
                transactionDao.getAllSync()
            }
            val assetIdMap = mutableMapOf<Long, Long>()

            val existingAssets = if (strategy == ImportStrategy.OVERWRITE) {
                emptyList()
            } else {
                smartAssetDao.getAllSync()
            }

            backup.assets.forEach { dto ->
                val existing = existingAssets.find {
                    it.name.equals(dto.name, ignoreCase = true) &&
                        formatDate(it.createdAtMillis) == dto.createdAt
                }
                val entity = assetEntityFromDto(dto)
                when {
                    strategy == ImportStrategy.OVERWRITE -> {
                        smartAssetDao.insert(entity.copy(id = dto.id))
                        assetIdMap[dto.id] = dto.id
                        added++
                    }
                    existing == null -> {
                        val newId = smartAssetDao.insert(entity)
                        assetIdMap[dto.id] = newId
                        added++
                    }
                    strategy == ImportStrategy.MERGE -> {
                        assetIdMap[dto.id] = existing.id
                        skipped++
                    }
                    else -> {
                        smartAssetDao.insert(entity.copy(id = existing.id))
                        assetIdMap[dto.id] = existing.id
                        updated++
                    }
                }
            }
            step++
            onProgress(step / totalSteps)

            if (strategy == ImportStrategy.OVERWRITE) {
                backup.assetUsageHistory.forEach { usage ->
                    val assetId = assetIdMap[usage.assetId] ?: usage.assetId
                    smartAssetDao.insertUsage(usageEntityFromDto(usage, assetId))
                    added++
                }
            } else {
                val existingUsages = smartAssetDao.getAllUsagesSync()
                backup.assetUsageHistory.forEach { usage ->
                    val assetId = assetIdMap[usage.assetId] ?: return@forEach
                    val usedAt = parseDateMillis(usage.date)
                    val duplicate = existingUsages.any {
                        it.smartAssetId == assetId &&
                            formatDate(it.usedAtMillis) == usage.date &&
                            it.savedAmountMinor == majorToMinor(usage.savedAmount)
                    }
                    if (duplicate && strategy == ImportStrategy.MERGE) {
                        skipped++
                    } else {
                        smartAssetDao.insertUsage(usageEntityFromDto(usage, assetId))
                        if (duplicate) updated++ else added++
                    }
                }
            }
            step++
            onProgress(step / totalSteps)

            val existingRecurring = if (strategy == ImportStrategy.OVERWRITE) {
                emptyList()
            } else {
                recurringDao.getAllSync()
            }
            backup.recurringTransactions.forEach { dto ->
                val categoryId = categoryIdMap[dto.categoryId] ?: return@forEach
                val existing = existingRecurring.find {
                    it.title.equals(dto.title, ignoreCase = true) &&
                        it.amountMinor == majorToMinor(dto.amount) &&
                        it.frequency.name == dto.frequency
                }
                val entity = recurringEntityFromDto(dto, categoryId)
                when {
                    strategy == ImportStrategy.OVERWRITE -> {
                        recurringDao.insertAll(listOf(entity.copy(id = dto.id)))
                        added++
                    }
                    existing == null -> {
                        recurringDao.insert(entity)
                        added++
                    }
                    strategy == ImportStrategy.MERGE -> skipped++
                    else -> {
                        recurringDao.insertAll(listOf(entity.copy(id = existing.id)))
                        updated++
                    }
                }
            }
            step++
            onProgress(step / totalSteps)

            backup.transactions.forEach { dto ->
                val categoryId = dto.categoryId?.let { categoryIdMap[it] }
                val smartAssetId = dto.smartAssetId?.let { assetIdMap[it] }
                val dateMillis = parseDateMillis(dto.date)
                val existing = existingTransactions.find {
                    it.amountMinor == majorToMinor(dto.amount) &&
                        formatDate(it.dateMillis) == dto.date &&
                        it.categoryId == categoryId &&
                        (it.note.orEmpty() == dto.note.orEmpty()) &&
                        (it.type == TransactionType.INCOME) == dto.isIncome
                }
                val entity = transactionEntityFromDto(dto, categoryId, smartAssetId, dateMillis)
                when {
                    strategy == ImportStrategy.OVERWRITE -> {
                        transactionDao.insert(entity.copy(id = dto.id))
                        added++
                    }
                    existing == null -> {
                        transactionDao.insert(entity)
                        added++
                    }
                    strategy == ImportStrategy.MERGE -> skipped++
                    else -> {
                        transactionDao.insert(entity.copy(id = existing.id))
                        updated++
                    }
                }
            }
            onProgress(1f)
        }

        return ImportResult(added = added, updated = updated, skipped = skipped)
    }

    private fun categoryEntityFromDto(dto: BackupCategoryDto, type: CategoryType): CategoryEntity =
        CategoryEntity(
            id = 0,
            name = dto.name.trim(),
            type = type,
            icon = dto.emoji,
            colorArgb = dto.colorHex?.let { hexToColorArgb(it) },
            isHidden = false,
            isSystem = false,
            sortOrder = dto.sortOrder,
            monthlyLimitMinor = null,
        )

    private fun transactionEntityFromDto(
        dto: BackupTransactionDto,
        categoryId: Long?,
        smartAssetId: Long?,
        dateMillis: Long,
    ): TransactionEntity = TransactionEntity(
        id = 0,
        type = if (dto.isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
        amountMinor = majorToMinor(dto.amount),
        categoryId = categoryId,
        smartAssetId = smartAssetId,
        note = dto.note,
        dateMillis = dateMillis,
        createdAtMillis = dateMillis,
    )

    private fun recurringEntityFromDto(
        dto: BackupRecurringDto,
        categoryId: Long,
    ): RecurringTransactionEntity = RecurringTransactionEntity(
        id = 0,
        title = dto.title,
        amountMinor = majorToMinor(dto.amount),
        categoryId = categoryId,
        isIncome = dto.isIncome,
        frequency = RecurringFrequency.valueOf(dto.frequency),
        dayOfMonth = dto.dayOfMonth,
        nextDateMillis = parseDateMillis(dto.nextDate),
        isActive = dto.isActive,
        note = dto.note,
    )

    private fun assetEntityFromDto(dto: BackupAssetDto): SmartAssetEntity = SmartAssetEntity(
        id = 0,
        name = dto.name,
        icon = dto.emoji,
        purchaseCostMinor = majorToMinor(dto.purchaseCost),
        alternativeCostMinor = majorToMinor(dto.savingsPerUse),
        trackingMode = runCatching { SmartAssetTrackingMode.valueOf(dto.trackingMode) }
            .getOrDefault(SmartAssetTrackingMode.MANUAL),
        status = runCatching { SmartAssetStatus.valueOf(dto.status) }
            .getOrDefault(SmartAssetStatus.PAYING_OFF),
        totalSavedMinor = majorToMinor(dto.totalSaved),
        totalUses = dto.usageCount,
        purchasedAtMillis = parseDateMillis(dto.createdAt),
        isActive = dto.isActive,
        note = dto.note,
        createdAtMillis = parseDateMillis(dto.createdAt),
    )

    private fun usageEntityFromDto(dto: BackupAssetUsageDto, assetId: Long): SmartAssetUsageEntity =
        SmartAssetUsageEntity(
            id = 0,
            smartAssetId = assetId,
            savedAmountMinor = majorToMinor(dto.savedAmount),
            usedAtMillis = parseDateMillis(dto.date),
            note = dto.note,
        )

    private fun resolvePeriodMillis(
        period: ExportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
    ): Pair<Long, Long> {
        if (period == ExportPeriod.CUSTOM && customStartMillis != null && customEndMillis != null) {
            return startOfDayMillis(customStartMillis) to endOfDayMillis(customEndMillis)
        }
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        return when (period) {
            ExportPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = startOfDayMillis(cal.timeInMillis)
                cal.add(Calendar.MONTH, 1)
                start to cal.timeInMillis
            }
            ExportPeriod.LAST_3_MONTHS -> {
                cal.add(Calendar.MONTH, -3)
                startOfDayMillis(cal.timeInMillis) to now
            }
            ExportPeriod.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = startOfDayMillis(cal.timeInMillis)
                cal.add(Calendar.YEAR, 1)
                start to cal.timeInMillis
            }
            ExportPeriod.ALL_TIME -> 0L to Long.MAX_VALUE
            ExportPeriod.CUSTOM -> startOfDayMillis(now - 30L * 86_400_000L) to now
        }
    }

    private fun minorToMajor(minor: Long): Double = minor / 100.0

    private fun majorToMinor(major: Double): Long = (major * 100.0).toLong()

    private fun formatDate(millis: Long): String =
        LocalDate.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

    private fun parseDateMillis(date: String): Long {
        val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun colorArgbToHex(argb: Long): String {
        val value = argb.toInt()
        return String.format(Locale.US, "#%08X", value)
    }

    private fun hexToColorArgb(hex: String): Long {
        val normalized = hex.removePrefix("#")
        val argb = when (normalized.length) {
            6 -> "FF$normalized"
            8 -> normalized
            else -> normalized
        }
        return argb.toLong(16)
    }
}
