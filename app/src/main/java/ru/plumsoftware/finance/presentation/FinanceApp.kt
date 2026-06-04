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
import androidx.compose.runtime.LaunchedEffect
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
import ru.plumsoftware.finance.presentation.accounts.AccountEditorScreen
import ru.plumsoftware.finance.presentation.accounts.AccountsScreen
import ru.plumsoftware.finance.presentation.addtransaction.AddTransactionScreen
import ru.plumsoftware.finance.presentation.analytics.AnalyticsScreen
import ru.plumsoftware.finance.presentation.achievements.AchievementsScreen
import ru.plumsoftware.finance.presentation.achievements.AchievementsViewModel
import ru.plumsoftware.finance.presentation.achievements.OwlAchievementToast
import ru.plumsoftware.finance.presentation.limits.LimitsScreen
import ru.plumsoftware.finance.presentation.categories.CategoriesScreen
import ru.plumsoftware.finance.presentation.categories.CategoryEditorScreen
import ru.plumsoftware.finance.presentation.dashboard.HomeScreen
import ru.plumsoftware.finance.presentation.export.ExportScreen
import ru.plumsoftware.finance.presentation.importdata.ImportPickerDeepLinkScreen
import ru.plumsoftware.finance.presentation.importdata.ImportPreviewScreen
import ru.plumsoftware.finance.presentation.history.HistoryScreen
import ru.plumsoftware.finance.presentation.goals.CreateGoalScreen
import ru.plumsoftware.finance.presentation.goals.GoalDetailScreen
import ru.plumsoftware.finance.presentation.goals.GoalsScreen
import ru.plumsoftware.finance.presentation.notifications.NotificationsScreen
import ru.plumsoftware.finance.presentation.onboarding.OnboardingScreen
import ru.plumsoftware.finance.presentation.permissions.PermissionsScreen
import ru.plumsoftware.finance.presentation.recurring.RecurringScreen
import ru.plumsoftware.finance.presentation.settings.AboutScreen
import ru.plumsoftware.finance.presentation.settings.SettingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.CreateSmartSavingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsDetailScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsScreen
import ru.plumsoftware.finance.presentation.smartsavings.SmartSavingsSnackbar
import ru.plumsoftware.finance.navigation.NavDeepLinks
import ru.plumsoftware.finance.navigation.navigateAppDeepLink
import ru.plumsoftware.finance.navigation.popBackStackOrHome
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.nav.BottomNavItems
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.FinanceTheme

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun FinanceApp(
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository = koinInject(),
    pendingImportLocalPath: String? = null,
    onPendingImportConsumed: () -> Unit = {},
    pendingDeepLinkIntent: android.content.Intent? = null,
    onPendingDeepLinkConsumed: () -> Unit = {},
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
        val achievementsViewModel: AchievementsViewModel = koinViewModel()
        val pendingAchievement by achievementsViewModel.pendingAchievement.collectAsStateWithLifecycle()
        val startRoute =
            if (isOnboardingCompleted == true) AppRoute.Home.route else AppRoute.Onboarding.route

        LaunchedEffect(pendingImportLocalPath, isOnboardingCompleted) {
            val path = pendingImportLocalPath ?: return@LaunchedEffect
            if (isOnboardingCompleted == true) {
                navController.navigate(AppRoute.ImportPreview.route(path)) {
                    launchSingleTop = true
                }
                onPendingImportConsumed()
            }
        }

        LaunchedEffect(pendingDeepLinkIntent, isOnboardingCompleted) {
            val intent = pendingDeepLinkIntent ?: return@LaunchedEffect
            if (isOnboardingCompleted == true) {
                navController.navigateAppDeepLink(intent)
                onPendingDeepLinkConsumed()
            }
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val bottomNavRoutes = BottomNavItems.map { it.route }
        val showBottomBar = currentRoute in bottomNavRoutes
        val colors = MaterialTheme.colorScheme
        val navigateFromBottomBar: (String) -> Unit = { route ->
            if (route == AppRoute.Home.route) {
                val navigatedToExistingHome = navController.popBackStack(AppRoute.Home.route, false)
                if (!navigatedToExistingHome) {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            } else {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

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
                            val goalsItem = BottomNavItems[1]
                            val analyticsItem = BottomNavItems[2]
                            val settingsItem = BottomNavItems[3]

                            listOf(homeItem, goalsItem).forEach { item ->
                                val isSelected =
                                    navBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { navigateFromBottomBar(item.route) },
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
                                    onClick = { navigateFromBottomBar(item.route) },
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
                    composable(
                        route = AppRoute.Onboarding.route,
                        deepLinks = listOf(NavDeepLinks.onboarding),
                    ) {
                        OnboardingScreen(
                            onComplete = {
                                navController.navigate(AppRoute.Home.route) {
                                    popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(
                        route = AppRoute.Home.route,
                        deepLinks = listOf(NavDeepLinks.home),
                    ) {
                        HomeScreen(
                            onOpenSmartSavingsClick = { navController.navigate(AppRoute.SmartSavings.route) },
                            onOpenGoalsClick = { navController.navigate(AppRoute.Goals.route) },
                            onOpenHistoryClick = { navController.navigate(AppRoute.History.route) },
                            onOpenAnalyticsClick = { navController.navigate(AppRoute.Analytics.route) },
                            onOpenLimitsClick = { navController.navigate(AppRoute.Limits.route) },
                            onOpenAchievementsClick = { navController.navigate(AppRoute.Achievements.route) },
                            onOpenNotificationsClick = { navController.navigate(AppRoute.Notifications.route) },
                            onCreateAssetClick = { navController.navigate(AppRoute.smartCreate(null)) },
                            onCreateGoalClick = { navController.navigate(AppRoute.goalCreate()) },
                            onGoalClick = { id -> navController.navigate(AppRoute.goalDetail(id)) },
                        )
                    }
                    composable(
                        route = AppRoute.History.route,
                        deepLinks = listOf(NavDeepLinks.history),
                    ) {
                        HistoryScreen(
                            onBack = { navController.popBackStackOrHome() },
                            onNavigateToAdd = { navController.navigate(AppRoute.AddTransaction.route) },
                        )
                    }
                    composable(
                        route = AppRoute.SmartSavings.route,
                        deepLinks = listOf(NavDeepLinks.smartSavings),
                    ) { backStackEntry ->
                        val snackbarMessage by backStackEntry.savedStateHandle
                            .getStateFlow<String?>(SmartSavingsSnackbar.KEY, null)
                            .collectAsStateWithLifecycle()
                        SmartSavingsScreen(
                            onBack = { navController.popBackStackOrHome() },
                            onCreateClick = {
                                // ИЗМЕНЕНО: Используем новую функцию-помощник
                                navController.navigate(AppRoute.smartCreate(null))
                            },
                            onOpenGoalsClick = { navController.navigate(AppRoute.Goals.route) },
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
                        deepLinks = listOf(NavDeepLinks.smartSavingsCreate, NavDeepLinks.smartSavingsCreateNew),
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
                            onBack = { navController.popBackStackOrHome() },
                            onCreated = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(SmartSavingsSnackbar.KEY, successMessage)
                                navController.popBackStackOrHome()
                            },
                        )
                    }
                    composable(
                        route = AppRoute.SMART_DETAIL,
                        deepLinks = listOf(NavDeepLinks.smartSavingsDetail),
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
                            onBack = { navController.popBackStackOrHome() },
                            onEdit = { id ->
                                navController.navigate(AppRoute.smartCreate(id))
                            },
                            onDeleteSuccess = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(SmartSavingsSnackbar.KEY, deletedMsg)
                                navController.popBackStackOrHome()
                            },
                        )
                    }
                    composable(
                        route = AppRoute.Goals.route,
                        deepLinks = listOf(NavDeepLinks.goals),
                    ) {
                        GoalsScreen(
                            onCreateClick = { navController.navigate(AppRoute.goalCreate()) },
                            onGoalClick = { id -> navController.navigate(AppRoute.goalDetail(id)) },
                        )
                    }
                    composable(
                        route = AppRoute.Achievements.route,
                        deepLinks = listOf(NavDeepLinks.achievements),
                    ) {
                        AchievementsScreen(
                            onBack = { navController.popBackStackOrHome() },
                            viewModel = achievementsViewModel,
                        )
                    }
                    composable(
                        route = AppRoute.GOAL_CREATE_WITH_ARGS,
                        deepLinks = listOf(NavDeepLinks.goalsCreate, NavDeepLinks.goalsCreateNew),
                        arguments = listOf(
                            navArgument("goalId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) {
                        CreateGoalScreen(
                            onBack = { navController.popBackStackOrHome() },
                            backLabel = stringResource(R.string.goals_title),
                        )
                    }
                    composable(
                        route = AppRoute.GOAL_DETAIL,
                        arguments = listOf(navArgument("goalId") { type = NavType.LongType }),
                    ) { goalEntry ->
                        val goalId = goalEntry.arguments?.getLong("goalId") ?: 0L
                        GoalDetailScreen(
                            goalId = goalId,
                            onBack = { navController.popBackStackOrHome() },
                            onEdit = { id -> navController.navigate(AppRoute.goalCreate(id)) },
                            onDeleted = { navController.popBackStackOrHome() },
                        )
                    }
                    composable(
                        route = AppRoute.Analytics.route,
                        deepLinks = listOf(NavDeepLinks.analytics),
                    ) {
                        AnalyticsScreen()
                    }
                    composable(
                        route = AppRoute.Limits.route,
                        deepLinks = listOf(NavDeepLinks.limits),
                    ) {
                        LimitsScreen(navController = navController)
                    }
                    composable(
                        route = AppRoute.Notifications.route,
                        deepLinks = listOf(NavDeepLinks.notifications),
                    ) {
                        NotificationsScreen(navController = navController)
                    }
                    composable(
                        route = AppRoute.Settings.route,
                        deepLinks = listOf(NavDeepLinks.settings),
                    ) {
                        SettingsScreen(
                            navController = navController,
                            onOpenAccounts = { navController.navigate(AppRoute.Accounts.route) },
                            onOpenCategories = { navController.navigate(AppRoute.Categories.route) },
                            onOpenLimits = { navController.navigate(AppRoute.Limits.route) },
                            onOpenGoals = { navController.navigate(AppRoute.Goals.route) },
                            onOpenAchievements = { navController.navigate(AppRoute.Achievements.route) },
                            onOpenRecurring = { navController.navigate(AppRoute.Recurring.route) },
                            onOpenExport = { navController.navigate(AppRoute.Export.route) },
                            onOpenPermissions = { navController.navigate(AppRoute.Permissions.route) },
                            onOpenAbout = { navController.navigate(AppRoute.About.route) },
                        )
                    }
                    composable(route = AppRoute.About.route) {
                        AboutScreen(
                            onBack = { navController.popBackStackOrHome() },
                        )
                    }
                    composable(
                        route = AppRoute.Permissions.route,
                        deepLinks = listOf(NavDeepLinks.permissions),
                    ) {
                        PermissionsScreen(navController = navController)
                    }
                    composable(
                        route = AppRoute.Export.route,
                        deepLinks = listOf(NavDeepLinks.export),
                    ) {
                        ExportScreen(navController = navController)
                    }
                    composable(
                        route = AppRoute.ImportPreview.route,
                        arguments = listOf(
                            navArgument("encodedPath") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val encodedPath = backStackEntry.arguments?.getString("encodedPath").orEmpty()
                        ImportPreviewScreen(
                            localFilePath = android.net.Uri.decode(encodedPath),
                            navController = navController,
                        )
                    }
                    composable(
                        route = AppRoute.ImportPicker.route,
                        deepLinks = listOf(NavDeepLinks.importPicker),
                    ) {
                        ImportPickerDeepLinkScreen(navController = navController)
                    }
                    composable(
                        route = AppRoute.Recurring.route,
                        deepLinks = listOf(NavDeepLinks.recurring),
                    ) {
                        RecurringScreen(navController = navController)
                    }
                    composable(route = AppRoute.Accounts.route) {
                        AccountsScreen(
                            onBack = { navController.popBackStackOrHome() },
                            onAdd = { navController.navigate(AppRoute.accountEdit()) },
                            onEdit = { id -> navController.navigate(AppRoute.accountEdit(id)) },
                        )
                    }
                    composable(
                        route = AppRoute.ACCOUNT_EDIT,
                        arguments = listOf(
                            navArgument("accountId") {
                                type = NavType.LongType
                                defaultValue = 0L
                            },
                        ),
                    ) { backStackEntry ->
                        val accountId = backStackEntry.arguments?.getLong("accountId")?.takeIf { it > 0L }
                        AccountEditorScreen(
                            accountId = accountId,
                            onBack = { navController.popBackStackOrHome() },
                            onSaved = { navController.popBackStackOrHome() },
                        )
                    }
                    composable(
                        route = AppRoute.Categories.route,
                        deepLinks = listOf(NavDeepLinks.categories),
                    ) {
                        CategoriesScreen(
                            onBack = { navController.popBackStackOrHome() },
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
                        deepLinks = listOf(NavDeepLinks.categoryEdit),
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
                            onBack = { navController.popBackStackOrHome() },
                        )
                    }
                    composable(
                        route = AppRoute.ADD_TRANSACTION_WITH_ARGS,
                        deepLinks = listOf(NavDeepLinks.addTransaction, NavDeepLinks.addTransactionWithQuickCategory),
                        arguments = listOf(
                            navArgument("quickCategory") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
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
                            onBack = { navController.popBackStackOrHome() },
                        )
                    }
                }
                pendingAchievement?.let { achievement ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (showBottomBar) 100.dp else Dimens.SpacingL),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        OwlAchievementToast(
                            achievement = achievement,
                            onDismiss = achievementsViewModel::clearPending,
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
