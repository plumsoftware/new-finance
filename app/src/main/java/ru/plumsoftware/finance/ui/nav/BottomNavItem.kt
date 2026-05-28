package ru.plumsoftware.finance.ui.nav

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
)
