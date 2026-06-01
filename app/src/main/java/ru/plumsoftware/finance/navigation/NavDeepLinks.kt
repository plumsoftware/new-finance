package ru.plumsoftware.finance.navigation

import androidx.navigation.navDeepLink

/** [androidx.navigation.navDeepLink] patterns for [AppDeepLinks] URIs. */
object NavDeepLinks {
    val onboarding = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/onboarding" }
    val home = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/home" }
    val history = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/history" }
    val analytics = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/analytics" }
    val settings = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/settings" }
    val smartSavings = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/smart_savings" }
    val smartSavingsCreate = navDeepLink {
        uriPattern = "${AppDeepLinks.BASE}/smart_savings/create?assetId={assetId}"
    }
    val smartSavingsCreateNew = navDeepLink {
        uriPattern = "${AppDeepLinks.BASE}/smart_savings/create"
    }
    val smartSavingsDetail = navDeepLink {
        uriPattern = "${AppDeepLinks.BASE}/smart_savings/detail/{assetId}"
    }
    val goals = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/goals" }
    val goalsCreate = navDeepLink {
        uriPattern = "${AppDeepLinks.BASE}/goals/create?goalId={goalId}"
    }
    val goalsCreateNew = navDeepLink {
        uriPattern = "${AppDeepLinks.BASE}/goals/create"
    }
    val limits = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/limits" }
    val notifications = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/notifications" }
    val addTransaction = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/add_transaction" }
    val addTransactionWithQuickCategory = navDeepLink {
        uriPattern = "${AppDeepLinks.BASE}/add_transaction?quickCategory={quickCategory}"
    }
    val categories = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/settings/categories" }
    val categoryEdit = navDeepLink {
        uriPattern = "${AppDeepLinks.BASE}/settings/categories/edit?categoryId={categoryId}&type={type}"
    }
    val export = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/settings/export" }
    val recurring = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/settings/recurring" }
    val permissions = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/settings/permissions" }
    val importPicker = navDeepLink { uriPattern = "${AppDeepLinks.BASE}/settings/import" }
}
