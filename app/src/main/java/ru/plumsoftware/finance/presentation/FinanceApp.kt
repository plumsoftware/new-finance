package ru.plumsoftware.finance.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.home.HomePlaceholderScreen
import ru.plumsoftware.finance.presentation.onboarding.OnboardingScreen
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.nav.BottomNavItems
import ru.plumsoftware.finance.ui.theme.IosBlue

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun FinanceApp(
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository = koinInject(),
) {
    val onboardingFlow = remember(settingsRepository.settings) {
        settingsRepository.settings.map { it.onboardingCompleted }
    }
    val isOnboardingCompleted by onboardingFlow.collectAsStateWithLifecycle(initialValue = null)

    if (isOnboardingCompleted == null) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    val startRoute = if (isOnboardingCompleted == true) AppRoute.Home.route else AppRoute.Onboarding.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in BottomNavItems.map { it.route }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp,
                        windowInsets = NavigationBarDefaults.windowInsets,
                    ) {
                        BottomNavItems.forEach { item ->
                            val isSelected =
                                navBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.background,
                                    selectedIconColor = IosBlue,
                                    selectedTextColor = IosBlue,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            composable(AppRoute.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.Home.route) { /* Тут будет DashboardScreen */ }
            composable(AppRoute.SmartSavings.route) { /* Тут будет SmartSavingsScreen */ }
            composable(AppRoute.Analytics.route) { /* Тут будет AnalyticsScreen */ }
            composable(AppRoute.Settings.route) { /* Тут будет SettingsScreen */ }
        }
    }
}