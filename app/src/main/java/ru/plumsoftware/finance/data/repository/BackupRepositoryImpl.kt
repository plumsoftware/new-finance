package ru.plumsoftware.finance.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.room.withTransaction
import ru.plumsoftware.finance.BuildConfig
import ru.plumsoftware.finance.data.backup.BackupFileWriter
import ru.plumsoftware.finance.data.backup.BackupSerializer
import ru.plumsoftware.finance.data.local.dao.AccountDao
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.local.dao.CategoryDao
import ru.plumsoftware.finance.data.local.dao.GoalDao
import ru.plumsoftware.finance.data.local.dao.RecurringTransactionDao
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.local.database.FinanceDatabase
import ru.plumsoftware.finance.data.local.entity.AccountEntity
import ru.plumsoftware.finance.data.local.entity.AchievementUnlockEntity
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.data.local.entity.GoalDepositEntity
import ru.plumsoftware.finance.data.local.entity.GoalEntity
import ru.plumsoftware.finance.data.local.entity.RecurringTransactionEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetUsageEntity
import ru.plumsoftware.finance.data.local.entity.TransactionEntity
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.BACKUP_FORMAT_VERSION
import ru.plumsoftware.finance.domain.model.AccountType
import ru.plumsoftware.finance.domain.model.BackupAccountDto
import ru.plumsoftware.finance.domain.model.BackupAssetDto
import ru.plumsoftware.finance.domain.model.BackupAssetUsageDto
import ru.plumsoftware.finance.domain.model.BackupCategoryDto
import ru.plumsoftware.finance.domain.model.BackupAchievementUnlockDto
import ru.plumsoftware.finance.domain.model.BackupGoalDepositDto
import ru.plumsoftware.finance.domain.model.BackupGoalDto
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BackupRepositoryImpl(
    private val context: Context,
    private val database: FinanceDatabase,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val recurringDao: RecurringTransactionDao,
    private val smartAssetDao: SmartAssetDao,
    private val goalDao: GoalDao,
    private val achievementUnlockDao: AchievementUnlockDao,
) : BackupRepository {

    override suspend fun buildBackup(
        period: ExportPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
    ): BackupModel {
        val (startMillis, endMillis) = resolvePeriodMillis(period, customStartMillis, customEndMillis)
        val accounts = accountDao.getAllActiveSync().map { it.toDomain() }
        val accountNameById = accounts.associate { it.id to it.name }
        val categories = categoryDao.getAllSync().map { it.toDomain() }
        val transactions = if (period == ExportPeriod.ALL_TIME) {
            transactionDao.getAllSync()
        } else {
            transactionDao.getByPeriodSync(startMillis, endMillis)
        }.map { it.toDomain() }
        val recurring = recurringDao.getAllSync().map { it.toDomain() }
        val assets = smartAssetDao.getAllSync().map { it.toDomain() }
        val usages = smartAssetDao.getAllUsagesSync().map { it.toDomain() }
        val goals = goalDao.getAllGoalsSync()
        val goalDeposits = goalDao.getAllDepositsSync()
        val achievementUnlocks = achievementUnlockDao.getAll()
        val limits = categories.mapNotNull { category ->
            category.monthlyLimitMinor?.let { limit ->
                BackupLimitDto(
                    categoryId = category.id,
                    monthlyLimit = minorToMajor(limit),
                )
            }
        }

        val backupAccounts = accounts.map { account ->
            BackupAccountDto(
                id = account.id,
                name = account.name,
                type = account.type.name,
                currencyCode = account.currencyCode,
                colorHex = account.colorHex,
                emoji = account.emoji,
                initialBalance = minorToMajor(account.initialBalanceMinor),
                sortOrder = account.sortOrder,
                isDefault = account.isDefault,
                isArchived = account.isArchived,
                createdAtMillis = account.createdAtMillis,
            )
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
                accountId = tx.accountId,
                accountName = accountNameById[tx.accountId],
                currencyCode = tx.currencyCode,
                originalAmount = minorToMajor(
                    if (tx.originalAmountMinor > 0L) tx.originalAmountMinor else tx.amountMinor,
                ),
                originalCurrencyCode = tx.originalCurrencyCode ?: tx.currencyCode,
                exchangeRate = tx.exchangeRate,
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
        val backupGoals = goals.map { goal ->
            BackupGoalDto(
                id = goal.id,
                name = goal.name,
                emoji = goal.emoji,
                targetAmount = minorToMajor(goal.targetAmountMinor),
                savedAmount = minorToMajor(goal.savedAmountMinor),
                colorHex = goal.colorHex,
                deadlineMillis = goal.deadline,
                note = goal.note,
                showOnHome = goal.showOnHome,
                isCompleted = goal.isCompleted,
                createdAtMillis = goal.createdAtMillis,
                currencyCode = goal.currencyCode,
                accountId = goal.accountId,
            )
        }
        val backupGoalDeposits = goalDeposits.map { deposit ->
            BackupGoalDepositDto(
                id = deposit.id,
                goalId = deposit.goalId,
                amount = minorToMajor(deposit.amountMinor),
                note = deposit.note,
                createdAtMillis = deposit.createdAtMillis,
                currencyCode = deposit.currencyCode,
                accountId = deposit.accountId,
            )
        }
        val backupAchievements = achievementUnlocks.map { unlock ->
            BackupAchievementUnlockDto(
                key = unlock.key,
                unlockedAtMillis = unlock.unlockedAtMillis,
            )
        }

        val meta = BackupMeta(
            version = BACKUP_FORMAT_VERSION,
            appVersion = BuildConfig.VERSION_NAME,
            exportedAt = formatExportedAt(System.currentTimeMillis()),
            deviceModel = Build.MODEL ?: "",
            recordCounts = BackupRecordCounts(
                accounts = backupAccounts.size,
                categories = backupCategories.size,
                transactions = backupTransactions.size,
                recurring = backupRecurring.size,
                assets = backupAssets.size,
                limits = limits.size,
                goals = backupGoals.size,
                goalDeposits = backupGoalDeposits.size,
                achievements = backupAchievements.size,
            ),
        )

        return BackupModel(
            meta = meta,
            accounts = backupAccounts,
            categories = backupCategories,
            transactions = backupTransactions,
            recurringTransactions = backupRecurring,
            assets = backupAssets,
            assetUsageHistory = backupUsages,
            limits = limits,
            goals = backupGoals,
            goalDeposits = backupGoalDeposits,
            achievements = backupAchievements,
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
            AccountRepositoryImpl(accountDao, transactionDao, context).ensureDefaultAccount()
            val defaultAccount = accountDao.getDefault()
                ?: error("Default account missing")

            if (strategy == ImportStrategy.OVERWRITE) {
                onProgress(0.05f)
                transactionDao.deleteAll()
                recurringDao.deleteAll()
                smartAssetDao.deleteAllUsages()
                smartAssetDao.deleteAllAssets()
                goalDao.deleteAllDeposits()
                goalDao.deleteAllGoals()
                categoryDao.deleteAll()
                achievementUnlockDao.deleteAll()
            }

            val accountIdMap = mutableMapOf<Long, Long>()
            backup.accounts.forEach { dto ->
                val existingByName = dto.name.takeIf { it.isNotBlank() }
                    ?.let { accountDao.findByName(it) }
                val entity = accountEntityFromDto(dto)
                when {
                    dto.isDefault -> {
                        accountIdMap[dto.id] = defaultAccount.id
                        accountDao.update(
                            entity.copy(
                                id = defaultAccount.id,
                                isDefault = true,
                                isArchived = false,
                            ),
                        )
                    }
                    existingByName == null -> {
                        val newId = accountDao.insert(entity.copy(isDefault = false))
                        accountIdMap[dto.id] = newId
                    }
                    else -> {
                        accountIdMap[dto.id] = existingByName.id
                        if (strategy != ImportStrategy.MERGE) {
                            accountDao.update(entity.copy(id = existingByName.id, isDefault = false))
                        }
                    }
                }
            }
            accountIdMap.putIfAbsent(1L, defaultAccount.id)

            val existingCategories = categoryDao.getAllSync()
            val categoryIdMap = mutableMapOf<Long, Long>()
            val totalSteps = 9f
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

            val goalIdMap = mutableMapOf<Long, Long>()
            val existingGoals = if (strategy == ImportStrategy.OVERWRITE) {
                emptyList()
            } else {
                goalDao.getAllGoalsSync()
            }
            backup.goals.forEach { dto ->
                val existing = existingGoals.find {
                    it.name.equals(dto.name, ignoreCase = true) &&
                        it.createdAtMillis == dto.createdAtMillis
                }
                val entity = goalEntityFromDto(dto)
                when {
                    strategy == ImportStrategy.OVERWRITE -> {
                        goalDao.insertGoal(entity.copy(id = dto.id))
                        goalIdMap[dto.id] = dto.id
                        added++
                    }
                    existing == null -> {
                        val newId = goalDao.insertGoal(entity)
                        goalIdMap[dto.id] = newId
                        added++
                    }
                    strategy == ImportStrategy.MERGE -> {
                        goalIdMap[dto.id] = existing.id
                        skipped++
                    }
                    else -> {
                        goalDao.updateGoal(entity.copy(id = existing.id))
                        goalIdMap[dto.id] = existing.id
                        updated++
                    }
                }
            }
            step++
            onProgress(step / totalSteps)

            val existingGoalDeposits = if (strategy == ImportStrategy.OVERWRITE) {
                emptyList()
            } else {
                goalDao.getAllDepositsSync()
            }
            backup.goalDeposits.forEach { dto ->
                val localGoalId = goalIdMap[dto.goalId] ?: return@forEach
                val duplicate = existingGoalDeposits.any {
                    it.goalId == localGoalId &&
                        it.createdAtMillis == dto.createdAtMillis &&
                        it.amountMinor == majorToMinor(dto.amount) &&
                        it.note.orEmpty() == dto.note.orEmpty()
                }
                if (duplicate && strategy == ImportStrategy.MERGE) {
                    skipped++
                } else {
                    goalDao.insertDeposit(goalDepositEntityFromDto(dto, localGoalId))
                    if (duplicate) updated++ else added++
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
                val localAccountId = accountIdMap[dto.accountId]
                    ?: dto.accountName?.let { accountDao.findByName(it)?.id }
                    ?: defaultAccount.id
                val entity = transactionEntityFromDto(
                    dto = dto,
                    categoryId = categoryId,
                    smartAssetId = smartAssetId,
                    dateMillis = dateMillis,
                    accountId = localAccountId,
                )
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
            step++
            onProgress(step / totalSteps)

            val existingAchievementKeys = if (strategy == ImportStrategy.OVERWRITE) {
                emptySet()
            } else {
                achievementUnlockDao.getAll().map { it.key }.toSet()
            }
            if (strategy == ImportStrategy.OVERWRITE && backup.achievements.isNotEmpty()) {
                achievementUnlockDao.insertAll(
                    backup.achievements.map { dto ->
                        AchievementUnlockEntity(
                            key = dto.key,
                            unlockedAtMillis = dto.unlockedAtMillis,
                        )
                    },
                )
                added += backup.achievements.size
            } else {
                backup.achievements.forEach { dto ->
                    val entity = AchievementUnlockEntity(
                        key = dto.key,
                        unlockedAtMillis = dto.unlockedAtMillis,
                    )
                    when {
                        dto.key in existingAchievementKeys && strategy == ImportStrategy.MERGE -> skipped++
                        dto.key in existingAchievementKeys -> {
                            achievementUnlockDao.insertAll(listOf(entity))
                            updated++
                        }
                        else -> {
                            achievementUnlockDao.insert(entity)
                            added++
                        }
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

    private fun accountEntityFromDto(dto: BackupAccountDto): AccountEntity = AccountEntity(
        id = 0,
        name = dto.name,
        type = runCatching { AccountType.valueOf(dto.type) }.getOrDefault(AccountType.DEBIT),
        currencyCode = dto.currencyCode,
        colorHex = dto.colorHex,
        emoji = dto.emoji,
        initialBalanceMinor = majorToMinor(dto.initialBalance),
        sortOrder = dto.sortOrder,
        isDefault = dto.isDefault,
        isArchived = dto.isArchived,
        createdAtMillis = dto.createdAtMillis,
    )

    private fun transactionEntityFromDto(
        dto: BackupTransactionDto,
        categoryId: Long?,
        smartAssetId: Long?,
        dateMillis: Long,
        accountId: Long,
    ): TransactionEntity {
        val amountMinor = majorToMinor(dto.amount)
        val originalMinor = majorToMinor(
            if (dto.originalAmount > 0.0) dto.originalAmount else dto.amount,
        )
        return TransactionEntity(
            id = 0,
            type = if (dto.isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
            amountMinor = amountMinor,
            categoryId = categoryId,
            smartAssetId = smartAssetId,
            note = dto.note,
            dateMillis = dateMillis,
            createdAtMillis = dateMillis,
            accountId = accountId,
            currencyCode = dto.currencyCode,
            originalAmountMinor = originalMinor,
            originalCurrencyCode = dto.originalCurrencyCode,
            exchangeRate = dto.exchangeRate,
        )
    }

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

    private fun goalEntityFromDto(dto: BackupGoalDto): GoalEntity = GoalEntity(
        id = 0,
        name = dto.name,
        emoji = dto.emoji,
        targetAmountMinor = majorToMinor(dto.targetAmount),
        savedAmountMinor = majorToMinor(dto.savedAmount),
        colorHex = dto.colorHex,
        deadline = dto.deadlineMillis,
        note = dto.note,
        showOnHome = dto.showOnHome,
        isCompleted = dto.isCompleted,
        createdAtMillis = dto.createdAtMillis,
        currencyCode = dto.currencyCode,
        accountId = dto.accountId,
    )

    private fun goalDepositEntityFromDto(
        dto: BackupGoalDepositDto,
        goalId: Long,
    ): GoalDepositEntity = GoalDepositEntity(
        id = 0,
        goalId = goalId,
        amountMinor = majorToMinor(dto.amount),
        note = dto.note,
        createdAtMillis = dto.createdAtMillis,
        currencyCode = dto.currencyCode,
        accountId = dto.accountId,
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

    private fun formatDate(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
    }

    private fun parseDateMillis(date: String): Long {
        val parts = date.split("-")
        require(parts.size == 3) { "Invalid date: $date" }
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun formatExportedAt(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(millis))

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
