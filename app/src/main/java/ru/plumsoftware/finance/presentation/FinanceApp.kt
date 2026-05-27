package ru.plumsoftware.finance.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.addtransaction.AddTransactionScreen
import ru.plumsoftware.finance.presentation.analytics.AnalyticsScreen
import ru.plumsoftware.finance.presentation.dashboard.DashboardScreen
import ru.plumsoftware.finance.presentation.onboarding.OnboardingScreen
import ru.plumsoftware.finance.presentation.settings.SettingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.CreateSmartSavingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsDetailScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsSnackbar
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.nav.BottomNavItems
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.IosBlue

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun FinanceApp(
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository = koinInject(),
) {
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = ru.plumsoftware.finance.domain.model.AppSettings(),
    )
    val darkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    FinanceTheme(darkTheme = darkTheme) {
        val onboardingFlow = remember(settingsRepository.settings) {
            settingsRepository.settings.map { it.onboardingCompleted }
        }
        val isOnboardingCompleted by onboardingFlow.collectAsStateWithLifecycle(initialValue = null)

        if (isOnboardingCompleted == null) {
            Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            return@FinanceTheme
        }

        val navController = rememberNavController()
        val startRoute = if (isOnboardingCompleted == true) AppRoute.Home.route else AppRoute.Onboarding.route

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val bottomNavRoutes = BottomNavItems.map { it.route }
        val showBottomBar = currentRoute in bottomNavRoutes
        val showHomeFab = currentRoute == AppRoute.Home.route

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AnimatedVisibility(visible = showBottomBar) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp,
                        )
                        NavigationBar(
                            containerColor = Color.Transparent,
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
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            ),
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.background,
                                        selectedIconColor = IosBlue,
                                        selectedTextColor = IosBlue,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    ),
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = startRoute,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp),
                ) {
                    composable(AppRoute.Onboarding.route) {
                        OnboardingScreen(
                            onComplete = {
                                navController.navigate(AppRoute.Home.route) {
                                    popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(AppRoute.Home.route) {
                        DashboardScreen()
                    }
                    composable(AppRoute.SmartSavings.route) { backStackEntry ->
                        val snackbarMessage by backStackEntry.savedStateHandle
                            .getStateFlow<String?>(SmartSavingsSnackbar.KEY, null)
                            .collectAsStateWithLifecycle()
                        SmartSavingsScreen(
                            onCreateClick = {
                                navController.navigate(AppRoute.SmartSavingsCreate.route)
                            },
                            onAssetClick = { id ->
                                navController.navigate(AppRoute.smartDetail(id))
                            },
                            snackbarMessage = snackbarMessage,
                            onSnackbarShown = {
                                backStackEntry.savedStateHandle.remove<String>(SmartSavingsSnackbar.KEY)
                            },
                        )
                    }
                    composable(
                        route = AppRoute.SmartSavingsCreate.route,
                        enterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(tween(280))
                        },
                        exitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut(tween(200))
                        },
                        popEnterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn(tween(280))
                        },
                        popExitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(tween(200))
                        },
                    ) { createEntry ->
                        val successMessage = stringResource(R.string.smart_created_success)
                        CreateSmartSavingsScreen(
                            viewModel = koinViewModel(viewModelStoreOwner = createEntry),
                            onBack = { navController.popBackStack() },
                            onCreated = {
                                runCatching {
                                    navController.getBackStackEntry(AppRoute.SmartSavings.route)
                                        .savedStateHandle[SmartSavingsSnackbar.KEY] = successMessage
                                }.onFailure {
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(SmartSavingsSnackbar.KEY, successMessage)
                                }
                                navController.navigate(AppRoute.SmartSavings.route) {
                                    popUpTo(AppRoute.SmartSavingsCreate.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable(
                        route = AppRoute.SMART_DETAIL,
                        arguments = listOf(navArgument("assetId") { type = NavType.LongType }),
                        enterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(tween(280))
                        },
                        exitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut(tween(200))
                        },
                        popEnterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn(tween(280))
                        },
                        popExitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(tween(200))
                        },
                    ) { entry ->
                        val assetId = entry.arguments?.getLong("assetId") ?: 0L
                        SmartSavingsDetailScreen(
                            assetId = assetId,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(AppRoute.Analytics.route) {
                        AnalyticsScreen()
                    }
                    composable(AppRoute.Settings.route) {
                        SettingsScreen()
                    }
                    composable(
                        route = AppRoute.AddTransaction.route,
                        enterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(tween(280))
                        },
                        exitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut(tween(200))
                        },
                        popEnterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn(tween(280))
                        },
                        popExitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(tween(200))
                        },
                    ) {
                        AddTransactionScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                if (showHomeFab) {
                    FloatingActionButton(
                        onClick = { navController.navigate(AppRoute.AddTransaction.route) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = DimensFabPadding,
                                bottom = innerPadding.calculateBottomPadding() + DimensFabPadding,
                            ),
                        containerColor = IosBlue,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_transaction),
                        )
                    }
                }
            }
        }
    }
}

private val DimensFabPadding = 20.dp
