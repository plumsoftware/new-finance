package ru.plumsoftware.finance.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import ru.plumsoftware.finance.data.firebase.NotificationDisplayHelper
import ru.plumsoftware.finance.navigation.AppDeepLinks.toAppNavigationRoute
import ru.plumsoftware.finance.ui.AppRoute

/**
 * In-app navigation for `finance://app/…` URIs.
 *
 * Always use [NavController.navigateAppDeepLink] instead of [NavController.handleDeepLink]:
 * handleDeepLink replaces the back stack and breaks the system «Назад» button.
 */
fun Intent.resolveAppDeepLinkUri(): Uri? {
    if (AppDeepLinks.isAppDeepLink(this)) return data
    val extras = extras ?: return null
    val dataMap = extras.keySet()
        .filterNot { key -> key.startsWith("google.") || key.startsWith("gcm.") }
        .associate { key -> key to extras.getString(key).orEmpty() }
    return parseNotificationDeepLink(dataMap)
}

fun NavController.navigateAppDeepLink(
    uri: Uri,
    fromExternalEntry: Boolean = false,
): Boolean {
    val route = uri.toAppNavigationRoute() ?: return false
    return runCatching {
        if (fromExternalEntry) {
            val homeRoute = AppRoute.Home.route
            if (currentDestination?.route != homeRoute) {
                navigate(homeRoute) {
                    popUpTo(graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        navigate(route) {
            launchSingleTop = true
        }
    }.isSuccess
}

/** Push / widget / cold start — keeps [AppRoute.Home] under the target for a working back stack. */
fun NavController.navigateAppDeepLink(intent: Intent): Boolean {
    val uri = intent.resolveAppDeepLinkUri() ?: return false
    return navigateAppDeepLink(uri, fromExternalEntry = true)
}

/** From in-app UI (e.g. notification history) — preserves the current back stack. */
fun NavController.navigateNotificationDeepLink(uri: Uri): Boolean =
    navigateAppDeepLink(uri, fromExternalEntry = false)

/**
 * Pops the back stack, or navigates to [AppRoute.Home] when the stack cannot pop
 * (e.g. after opening a screen from a push deep link).
 */
fun NavController.popBackStackOrHome(): Boolean {
    if (popBackStack()) return true
    return runCatching {
        navigate(AppRoute.Home.route) {
            popUpTo(graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }.isSuccess
}

fun NavController.previousRouteBackLabelRes(): Int = when (previousBackStackEntry?.destination?.route) {
    AppRoute.Settings.route -> ru.plumsoftware.finance.R.string.categories_back_settings
    AppRoute.Notifications.route -> ru.plumsoftware.finance.R.string.notifications
    AppRoute.Home.route -> ru.plumsoftware.finance.R.string.nav_home
    else -> ru.plumsoftware.finance.R.string.nav_home
}

/** Returns a navigable URI only when [NotificationDisplayHelper.DATA_KEY_DEEP_LINK] is set and valid. */
fun parseNotificationDeepLink(data: Map<String, String>): Uri? {
    val raw = data[NotificationDisplayHelper.DATA_KEY_DEEP_LINK]?.trim().orEmpty()
    if (raw.isEmpty()) return null
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return null
    return uri.takeIf { AppDeepLinks.isAppDeepLink(it) }
}
