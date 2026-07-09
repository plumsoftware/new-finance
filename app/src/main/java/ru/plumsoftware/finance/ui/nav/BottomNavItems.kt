package ru.plumsoftware.finance.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.AppRoute

val BottomNavItems = listOf(
    BottomNavItem(AppRoute.Home.route, R.string.nav_home, Icons.Outlined.Home),
    BottomNavItem(AppRoute.Tools.route, R.string.nav_tools, Icons.Outlined.Calculate),
    BottomNavItem(AppRoute.Analytics.route, R.string.nav_analytics, Icons.Outlined.Analytics),
    BottomNavItem(AppRoute.Settings.route, R.string.nav_settings, Icons.Outlined.Settings),
)
