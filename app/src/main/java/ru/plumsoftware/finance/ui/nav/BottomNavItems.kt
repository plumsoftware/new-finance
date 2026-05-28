package ru.plumsoftware.finance.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import ru.plumsoftware.finance.ui.AppRoute

val BottomNavItems = listOf(
    BottomNavItem(AppRoute.Home.route, "Главная", Icons.Outlined.Home),
    BottomNavItem(AppRoute.History.route, "История", Icons.Outlined.History),
    BottomNavItem(AppRoute.Analytics.route, "Аналитика", Icons.Outlined.Analytics),
    BottomNavItem(AppRoute.Settings.route, "Настройки", Icons.Outlined.Settings),
)