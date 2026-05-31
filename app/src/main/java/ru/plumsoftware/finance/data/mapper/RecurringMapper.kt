package ru.plumsoftware.finance.data.mapper

import ru.plumsoftware.finance.data.local.entity.RecurringTransactionEntity
import ru.plumsoftware.finance.domain.model.RecurringTransaction

fun RecurringTransactionEntity.toDomain(): RecurringTransaction = RecurringTransaction(
    id = id,
    title = title,
    amountMinor = amountMinor,
    categoryId = categoryId,
    isIncome = isIncome,
    frequency = frequency,
    dayOfMonth = dayOfMonth,
    nextDateMillis = nextDateMillis,
    isActive = isActive,
    note = note,
)

fun RecurringTransaction.toEntity(): RecurringTransactionEntity = RecurringTransactionEntity(
    id = id,
    title = title,
    amountMinor = amountMinor,
    categoryId = categoryId,
    isIncome = isIncome,
    frequency = frequency,
    dayOfMonth = dayOfMonth,
    nextDateMillis = nextDateMillis,
    isActive = isActive,
    note = note,
)
