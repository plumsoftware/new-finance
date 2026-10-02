package ru.plumsoftware.finance.ui.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
)
