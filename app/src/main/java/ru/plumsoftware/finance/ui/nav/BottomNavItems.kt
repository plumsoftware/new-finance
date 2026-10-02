package ru.plumsoftware.finance.ui.nav

import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.AppRoute

/** Корневые вкладки (§5.1): Главная · Расчёты · [FAB] · Аналитика · Настройки. */
val BottomNavItems = listOf(
    BottomNavItem(AppRoute.Home.route, R.string.nav_home, R.drawable.ic_nav_home),
    BottomNavItem(AppRoute.Tools.route, R.string.nav_tools, R.drawable.ic_nav_calc),
    BottomNavItem(AppRoute.Analytics.route, R.string.nav_analytics, R.drawable.ic_nav_analytics),
    BottomNavItem(AppRoute.Settings.route, R.string.nav_settings, R.drawable.ic_nav_settings),
)
