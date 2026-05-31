package ru.plumsoftware.finance.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.addtransaction.AddTransactionScreen
import ru.plumsoftware.finance.presentation.analytics.AnalyticsScreen
import ru.plumsoftware.finance.presentation.limits.LimitsScreen
import ru.plumsoftware.finance.presentation.categories.CategoriesScreen
import ru.plumsoftware.finance.presentation.categories.CategoryEditorScreen
import ru.plumsoftware.finance.presentation.dashboard.HomeScreen
import ru.plumsoftware.finance.presentation.export.ExportScreen
import ru.plumsoftware.finance.presentation.history.HistoryScreen
import ru.plumsoftware.finance.presentation.notifications.NotificationsScreen
import ru.plumsoftware.finance.presentation.onboarding.OnboardingScreen
import ru.plumsoftware.finance.presentation.permissions.PermissionsScreen
import ru.plumsoftware.finance.presentation.recurring.RecurringScreen
import ru.plumsoftware.finance.presentation.settings.SettingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.CreateSmartSavingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsDetailScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsSnackbar
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.nav.BottomNavItems
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.FinanceTheme

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
            Box(modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background))
            return@FinanceTheme
        }

        val navController = rememberNavController()
        val startRoute =
            if (isOnboardingCompleted == true) AppRoute.Home.route else AppRoute.Onboarding.route

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val bottomNavRoutes = BottomNavItems.map { it.route }
        val showBottomBar = currentRoute in bottomNavRoutes
        val colors = MaterialTheme.colorScheme

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AnimatedVisibility(visible = showBottomBar) {
                    Box {
                        HorizontalDivider(
                            color = colors.outline,
                            thickness = Dimens.dividerThickness,
                        )
                        NavigationBar(
                            containerColor = colors.surface,
                            tonalElevation = 0.dp,
                            windowInsets = NavigationBarDefaults.windowInsets,
                        ) {
                            val homeItem = BottomNavItems[0]
                            val historyItem = BottomNavItems[1]
                            val analyticsItem = BottomNavItems[2]
                            val settingsItem = BottomNavItems[3]

                            listOf(homeItem, historyItem).forEach { item ->
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
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = stringResource(item.titleRes),
                                        )
                                    },
                                    label = {
                                        if (isSelected) {
                                            Text(
                                                text = stringResource(item.titleRes),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                            )
                                        }
                                    },
                                    alwaysShowLabel = isSelected,
                                    colors = navBarItemColors(isSelected),
                                )
                            }
                            NavigationBarItem(
                                selected = false,
                                onClick = { navController.navigate(AppRoute.AddTransaction.route) },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-8).dp)
                                            .size(Dimens.ButtonHeight)
                                            .shadow(8.dp, CircleShape)
                                            .background(colors.primary, CircleShape)
                                            .clickable { navController.navigate(AppRoute.AddTransaction.route) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = stringResource(R.string.add_transaction),
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                },
                                label = {},
                                alwaysShowLabel = false,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent,
                                ),
                            )
                            listOf(analyticsItem, settingsItem).forEach { item ->
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
                                    icon = {
                                        Icon(
                                            item.icon,
                                            contentDescription = stringResource(item.titleRes),
                                        )
                                    },
                                    label = {
                                        if (isSelected) {
                                            Text(
                                                text = stringResource(item.titleRes),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                ),
                                            )
                                        }
                                    },
                                    alwaysShowLabel = isSelected,
                                    colors = navBarItemColors(isSelected),
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
                        HomeScreen(
                            onOpenSmartSavingsClick = { navController.navigate(AppRoute.SmartSavings.route) },
                            onOpenHistoryClick = { navController.navigate(AppRoute.History.route) },
                            onOpenAnalyticsClick = { navController.navigate(AppRoute.Analytics.route) },
                            onOpenLimitsClick = { navController.navigate(AppRoute.Limits.route) },
                            onOpenNotificationsClick = { navController.navigate(AppRoute.Notifications.route) },
                            onCreateAssetClick = { navController.navigate(AppRoute.smartCreate(null)) },
                        )
                    }
                    composable(AppRoute.History.route) {
                        HistoryScreen(
                            onNavigateToAdd = { navController.navigate(AppRoute.AddTransaction.route) },
                        )
                    }
                    composable(AppRoute.SmartSavings.route) { backStackEntry ->
                        val snackbarMessage by backStackEntry.savedStateHandle
                            .getStateFlow<String?>(SmartSavingsSnackbar.KEY, null)
                            .collectAsStateWithLifecycle()
                        SmartSavingsScreen(
                            onBack = { navController.popBackStack() },
                            onCreateClick = {
                                // ИЗМЕНЕНО: Используем новую функцию-помощник
                                navController.navigate(AppRoute.smartCreate(null))
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
                        route = AppRoute.SMART_CREATE_WITH_ARGS,
                        arguments = listOf(navArgument("assetId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }),
                        enterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(
                                tween(280)
                            )
                        },
                        exitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut(
                                tween(200)
                            )
                        },
                        popEnterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn(
                                tween(280)
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(
                                tween(200)
                            )
                        },
                    ) { createEntry ->
                        // Достаем ID (если он есть, значит это Редактирование, если нет - Создание)
                        val assetIdStr = createEntry.arguments?.getString("assetId")
                        val assetId = assetIdStr?.toLongOrNull()

                        // Динамическое сообщение для снэкбара
                        val successMessage =
                            if (assetId != null) stringResource(R.string.smart_updated_success)
                            else stringResource(R.string.smart_created_success)

                        CreateSmartSavingsScreen(

                            viewModel = koinViewModel(viewModelStoreOwner = createEntry) {
                                parametersOf(
                                    assetId
                                )
                            },
                            onBack = { navController.popBackStack() },
                            onCreated = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(SmartSavingsSnackbar.KEY, successMessage)
                                navController.popBackStack()
                            },
                        )
                    }
                    composable(
                        route = AppRoute.SMART_DETAIL,
                        arguments = listOf(navArgument("assetId") { type = NavType.LongType }),
                        enterTransition = { slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(tween(280)) },
                        exitTransition = { slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut(tween(200)) },
                        popEnterTransition = { slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn(tween(280)) },
                        popExitTransition = { slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(tween(200)) },
                    ) { entry ->
                        val assetId = entry.arguments?.getLong("assetId") ?: 0L
                        val deletedMsg = stringResource(R.string.smart_deleted_success)
                        SmartSavingsDetailScreen(
                            assetId = assetId,
                            onBack = { navController.popBackStack() },
                            onEdit = { id ->
                                navController.navigate(AppRoute.smartCreate(id))
                            },
                            onDeleteSuccess = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(SmartSavingsSnackbar.KEY, deletedMsg)
                                navController.popBackStack()
                            },
                        )
                    }
                    composable(AppRoute.Analytics.route) {
                        AnalyticsScreen()
                    }
                    composable(AppRoute.Limits.route) {
                        LimitsScreen(navController = navController)
                    }
                    composable(AppRoute.Notifications.route) {
                        NotificationsScreen(navController = navController)
                    }
                    composable(AppRoute.Settings.route) {
                        SettingsScreen(
                            onOpenCategories = { navController.navigate(AppRoute.Categories.route) },
                            onOpenLimits = { navController.navigate(AppRoute.Limits.route) },
                            onOpenRecurring = { navController.navigate(AppRoute.Recurring.route) },
                            onOpenExport = { navController.navigate(AppRoute.Export.route) },
                            onOpenPermissions = { navController.navigate(AppRoute.Permissions.route) },
                        )
                    }
                    composable(AppRoute.Permissions.route) {
                        PermissionsScreen(navController = navController)
                    }
                    composable(AppRoute.Export.route) {
                        ExportScreen(navController = navController)
                    }
                    composable(AppRoute.Recurring.route) {
                        RecurringScreen(navController = navController)
                    }
                    composable(AppRoute.Categories.route) {
                        CategoriesScreen(
                            onBack = { navController.popBackStack() },
                            onAdd = { type ->
                                navController.navigate(AppRoute.categoryEdit(type = type.name))
                            },
                            onEdit = { id ->
                                navController.navigate(AppRoute.categoryEdit(categoryId = id))
                            },
                        )
                    }
                    composable(
                        route = AppRoute.CATEGORY_EDIT,
                        arguments = listOf(
                            navArgument("categoryId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                            navArgument("type") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) {
                        CategoryEditorScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(
                        route = AppRoute.AddTransaction.route,
                        enterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(
                                tween(
                                    280
                                )
                            )
                        },
                        exitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { it } + fadeOut(
                                tween(
                                    200
                                )
                            )
                        },
                        popEnterTransition = {
                            slideInHorizontally(animationSpec = tween(280)) { -it } + fadeIn(
                                tween(
                                    280
                                )
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(animationSpec = tween(240)) { -it } + fadeOut(
                                tween(
                                    200
                                )
                            )
                        },
                    ) {
                        AddTransactionScreen(
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun navBarItemColors(isSelected: Boolean) = NavigationBarItemDefaults.colors(
    indicatorColor = Color.Transparent,
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
