package ru.plumsoftware.finance.domain.model

import androidx.annotation.StringRes
import ru.plumsoftware.finance.R

enum class RecurringFrequency(@StringRes val labelRes: Int) {
    DAILY(R.string.freq_daily),
    WEEKLY(R.string.freq_weekly),
    MONTHLY(R.string.freq_monthly),
    YEARLY(R.string.freq_yearly),
}

data class RecurringTransaction(
    val id: Long = 0,
    val title: String,
    val amountMinor: Long,
    val categoryId: Long,
    val isIncome: Boolean,
    val frequency: RecurringFrequency,
    val dayOfMonth: Int? = null,
    val nextDateMillis: Long,
    val isActive: Boolean = true,
    val note: String? = null,
)
