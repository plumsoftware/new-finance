package ru.plumsoftware.finance.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import ru.plumsoftware.finance.ui.AppRoute

val BottomNavItems = listOf(
    BottomNavItem(AppRoute.Home.route, "Главная", Icons.Default.Home),
    BottomNavItem(AppRoute.SmartSavings.route, "Экономия", Icons.Default.Savings),
    BottomNavItem(AppRoute.Analytics.route, "Аналитика", Icons.Default.Analytics),
    BottomNavItem(AppRoute.Settings.route, "Настройки", Icons.Default.Settings)
)