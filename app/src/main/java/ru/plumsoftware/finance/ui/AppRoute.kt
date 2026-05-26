package ru.plumsoftware.finance.ui

sealed class AppRoute(val route: String) {
    object Onboarding : AppRoute("onboarding")
    object Home : AppRoute("home") // Дашборд
    object SmartSavings : AppRoute("smart_savings") // Киллер-фича
    object Analytics : AppRoute("analytics")
    object Settings : AppRoute("settings")
    object AddTransaction : AppRoute("add_transaction")
}