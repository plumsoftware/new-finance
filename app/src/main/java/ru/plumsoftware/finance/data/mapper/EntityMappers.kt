package ru.plumsoftware.finance.data.mapper

import ru.plumsoftware.finance.data.local.dao.CategorySpendingRow
import ru.plumsoftware.finance.data.local.dao.DailySummaryRow
import ru.plumsoftware.finance.data.local.entity.CategoryEntity
import ru.plumsoftware.finance.data.local.entity.GoalDepositEntity
import ru.plumsoftware.finance.data.local.entity.GoalEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetEntity
import ru.plumsoftware.finance.data.local.entity.SmartAssetUsageEntity
import ru.plumsoftware.finance.data.local.entity.TransactionEntity
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.domain.model.DailySummary
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.GoalDeposit
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetUsage
import ru.plumsoftware.finance.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = type,
    amountMinor = amountMinor,
    categoryId = categoryId,
    smartAssetId = smartAssetId,
    note = note,
    dateMillis = dateMillis,
    createdAtMillis = createdAtMillis,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type,
    amountMinor = amountMinor,
    categoryId = categoryId,
    smartAssetId = smartAssetId,
    note = note,
    dateMillis = dateMillis,
    createdAtMillis = createdAtMillis,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    type = type,
    icon = icon,
    colorArgb = colorArgb,
    isHidden = isHidden,
    isSystem = isSystem,
    sortOrder = sortOrder,
    monthlyLimitMinor = monthlyLimitMinor,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    type = type,
    icon = icon,
    colorArgb = colorArgb,
    isHidden = isHidden,
    isSystem = isSystem,
    sortOrder = sortOrder,
    monthlyLimitMinor = monthlyLimitMinor,
)

fun SmartAssetEntity.toDomain(): SmartAsset = SmartAsset(
    id = id,
    name = name,
    icon = icon,
    purchaseCostMinor = purchaseCostMinor,
    alternativeCostMinor = alternativeCostMinor,
    trackingMode = trackingMode,
    status = status,
    totalSavedMinor = totalSavedMinor,
    totalUses = totalUses,
    purchasedAtMillis = purchasedAtMillis,
    isActive = isActive,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun SmartAsset.toEntity(): SmartAssetEntity = SmartAssetEntity(
    id = id,
    name = name,
    icon = icon,
    purchaseCostMinor = purchaseCostMinor,
    alternativeCostMinor = alternativeCostMinor,
    trackingMode = trackingMode,
    status = status,
    totalSavedMinor = totalSavedMinor,
    totalUses = totalUses,
    purchasedAtMillis = purchasedAtMillis,
    isActive = isActive,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun SmartAssetUsageEntity.toDomain(): SmartAssetUsage = SmartAssetUsage(
    id = id,
    smartAssetId = smartAssetId,
    savedAmountMinor = savedAmountMinor,
    usedAtMillis = usedAtMillis,
    note = note,
)

fun SmartAssetUsage.toEntity(): SmartAssetUsageEntity = SmartAssetUsageEntity(
    id = id,
    smartAssetId = smartAssetId,
    savedAmountMinor = savedAmountMinor,
    usedAtMillis = usedAtMillis,
    note = note,
)

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    name = name,
    emoji = emoji,
    targetAmountMinor = targetAmountMinor,
    savedAmountMinor = savedAmountMinor,
    colorHex = colorHex,
    deadline = deadline,
    note = note,
    showOnHome = showOnHome,
    isCompleted = isCompleted,
    createdAtMillis = createdAtMillis,
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    name = name,
    emoji = emoji,
    targetAmountMinor = targetAmountMinor,
    savedAmountMinor = savedAmountMinor,
    colorHex = colorHex,
    deadline = deadline,
    note = note,
    showOnHome = showOnHome,
    isCompleted = isCompleted,
    createdAtMillis = createdAtMillis,
)

fun GoalDepositEntity.toDomain(): GoalDeposit = GoalDeposit(
    id = id,
    goalId = goalId,
    amountMinor = amountMinor,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun GoalDeposit.toEntity(): GoalDepositEntity = GoalDepositEntity(
    id = id,
    goalId = goalId,
    amountMinor = amountMinor,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun DailySummaryRow.toDomain(): DailySummary = DailySummary(
    dateMillis = dayStartMillis,
    incomeMinor = incomeMinor,
    expenseMinor = expenseMinor,
)

fun Category.toSpending(amountMinor: Long, totalExpenseMinor: Long): CategorySpending =
    CategorySpending(
        category = this,
        amountMinor = amountMinor,
        sharePercent = if (totalExpenseMinor > 0) {
            amountMinor.toFloat() / totalExpenseMinor * 100f
        } else {
            0f
        },
    )
