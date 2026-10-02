package ru.plumsoftware.finance.presentation.common

import androidx.compose.ui.graphics.Color
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import java.time.LocalDate

/** Операция для списков (§6.1.9, §6.3). */
data class TxItem(
    val id: Long,
    val title: String,
    val categoryName: String,
    val emoji: String,
    val color: Color,
    val date: LocalDate,
    val time: String,
    val amountMinor: Long,
    val type: TransactionType,
    val accountName: String?,
    val currencyCode: String,
    val note: String?,
) {
    val isIncome: Boolean get() = type == TransactionType.INCOME
}

object CategoryColors {
    val Fallback = Color(0xFF007AFF)
    val SavingsColor = Color(0xFF5856D6)

    fun of(category: Category?): Color =
        category?.colorArgb?.let { Color(it.toInt()).copy(alpha = 1f) } ?: Fallback
}

fun Transaction.toTxItem(
    categories: Map<Long, Category>,
    accounts: Map<Long, Account> = emptyMap(),
    savingsLabel: String = "",
    noCategoryLabel: String = "",
): TxItem {
    val category = categoryId?.let { categories[it] }
    val isSavings = type == TransactionType.SAVINGS
    val catName = when {
        category != null -> category.name
        isSavings -> savingsLabel
        else -> noCategoryLabel
    }
    return TxItem(
        id = id,
        title = note?.takeIf { it.isNotBlank() } ?: catName,
        categoryName = catName,
        emoji = category?.icon ?: if (isSavings) "🎯" else "💸",
        color = if (isSavings) CategoryColors.SavingsColor else CategoryColors.of(category),
        date = DateFmt.toLocalDate(dateMillis),
        time = DateFmt.time(dateMillis),
        amountMinor = amountMinor,
        type = type,
        accountName = accounts[accountId]?.name,
        currencyCode = currencyCode,
        note = note,
    )
}
