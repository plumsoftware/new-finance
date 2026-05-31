package ru.plumsoftware.finance.ui.theme

import androidx.annotation.DrawableRes
import ru.plumsoftware.finance.R

/**
 * Маскоты для пустых состояний (не онбординг).
 * Замените drawable на свои `mascot_empty_*`, когда будут готовы.
 */
object MascotAssets {
    @DrawableRes
    val emptyTransactions: Int = R.drawable.mascot_empty_transactions

    @DrawableRes
    val emptySmartSavings: Int = R.drawable.mascot_empty_smart_savings

    @DrawableRes
    val emptyAnalytics: Int = R.drawable.mascot_empty_analytics

    @DrawableRes
    val emptyNotifications: Int = R.drawable.mascot_empty_notifications
}
