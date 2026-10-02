package ru.plumsoftware.finance.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.nav.BottomNavItems

/**
 * Переходы экранов — Material shared axis X (ТЗ §3.5): сдвиг ~30dp + затухание, 300 мс.
 * «Новая операция» анимируется сама (выезд снизу), остальные экраны рядом с ней только затухают.
 */
object SharedAxisX {
    private const val DURATION = 300
    private const val FADE_OUT = 90

    /** ~30dp на телефоне 360dp: доля ширины, чтобы не зависеть от плотности. */
    private fun offset(fullWidth: Int) = fullWidth / 12

    private val tabRoutes = BottomNavItems.map { it.route }

    private fun NavBackStackEntry.route() = destination.route.orEmpty()

    private fun isSheetRoute(route: String) = route.startsWith(AppRoute.AddTransaction.route)

    /** Направление вперёд: для вкладок — по их порядку, для остальных — переход вглубь. */
    private fun AnimatedContentTransitionScope<NavBackStackEntry>.forward(pop: Boolean): Boolean {
        val from = tabRoutes.indexOf(initialState.route())
        val to = tabRoutes.indexOf(targetState.route())
        return if (from >= 0 && to >= 0) to > from else !pop
    }

    private fun AnimatedContentTransitionScope<NavBackStackEntry>.involvesSheet() =
        isSheetRoute(initialState.route()) || isSheetRoute(targetState.route())

    private fun enter(scope: AnimatedContentTransitionScope<NavBackStackEntry>, pop: Boolean): EnterTransition {
        if (scope.involvesSheet()) return fadeIn(tween(150))
        val sign = if (scope.forward(pop)) 1 else -1
        return slideInHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { sign * offset(it) } +
            fadeIn(tween(DURATION - FADE_OUT, delayMillis = FADE_OUT, easing = LinearOutSlowInEasing))
    }

    private fun exit(scope: AnimatedContentTransitionScope<NavBackStackEntry>, pop: Boolean): ExitTransition {
        if (scope.involvesSheet()) return fadeOut(tween(150))
        val sign = if (scope.forward(pop)) 1 else -1
        return slideOutHorizontally(tween(DURATION, easing = FastOutSlowInEasing)) { -sign * offset(it) } +
            fadeOut(tween(FADE_OUT, easing = FastOutLinearInEasing))
    }

    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = { enter(this, pop = false) }
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = { exit(this, pop = false) }
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = { enter(this, pop = true) }
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = { exit(this, pop = true) }
}
