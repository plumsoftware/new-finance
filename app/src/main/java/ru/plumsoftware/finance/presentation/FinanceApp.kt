package ru.plumsoftware.finance.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.ThemeMode
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.accounts.AccountEditorScreen
import ru.plumsoftware.finance.presentation.accounts.AccountsScreen
import ru.plumsoftware.finance.presentation.addtransaction.AddTransactionScreen
import ru.plumsoftware.finance.presentation.analytics.AnalyticsScreen
import ru.plumsoftware.finance.presentation.achievements.AchievementsScreen
import ru.plumsoftware.finance.presentation.achievements.AchievementsViewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import ru.plumsoftware.finance.ui.ds.Kopi
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
import ru.plumsoftware.finance.navigation.SharedAxisX
import ru.plumsoftware.finance.navigation.navigateAppDeepLink
import ru.plumsoftware.finance.navigation.popBackStackOrHome
import ru.plumsoftware.finance.presentation.tools.CreditCalculatorScreen
import ru.plumsoftware.finance.presentation.tools.DepositCalculatorScreen
import ru.plumsoftware.finance.presentation.tools.EarlyRepaymentCalculatorScreen
import ru.plumsoftware.finance.presentation.tools.GoalCalculatorScreen
import ru.plumsoftware.finance.presentation.tools.MortgageCalculatorScreen
import ru.plumsoftware.finance.presentation.tools.RentVsBuyScreen
import ru.plumsoftware.finance.presentation.tools.SavingsAccountCalculatorScreen
import ru.plumsoftware.finance.presentation.tools.ToolsScreen
import ru.plumsoftware.finance.presentation.tools.SavedCalculationsScreen
import ru.plumsoftware.finance.ui.AppRoute
import ru.plumsoftware.finance.ui.nav.BottomNavItems
import ru.plumsoftware.finance.ui.nav.FinanceBottomBar
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.AmountVisibility
import ru.plumsoftware.finance.ui.ds.LocalAmountVisibility
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.first
import ru.plumsoftware.finance.ui.ds.MascotSnackbarHost
import ru.plumsoftware.finance.ui.ds.MascotSnackbarState
import androidx.compose.runtime.CompositionLocalProvider
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.FinanceTheme

private val SheetEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

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
        initialValue = AppSettings(),
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

        val mascotSnackbar = remember { MascotSnackbarState() }
        // Скрытие сумм: начальное значение — из настройки при запуске, далее — по касанию (§6.13).
        var amountsHidden by rememberSaveable { mutableStateOf<Boolean?>(null) }
        LaunchedEffect(Unit) {
            if (amountsHidden == null) amountsHidden = settingsRepository.settings.first().hideAmountsOnLaunch
        }
        val amountVisibility = AmountVisibility(amountsHidden == true) { amountsHidden = amountsHidden != true }
        CompositionLocalProvider(
            LocalMascotSnackbar provides mascotSnackbar,
            LocalAmountVisibility provides amountVisibility,
        ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(100)),
                ) {
                    FinanceBottomBar(
                        selectedRoute = currentRoute,
                        onSelect = navigateFromBottomBar,
                        onFab = { navController.navigate(AppRoute.addTransaction()) },
                    )
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
                    enterTransition = SharedAxisX.enterTransition,
                    exitTransition = SharedAxisX.exitTransition,
                    popEnterTransition = SharedAxisX.popEnterTransition,
                    popExitTransition = SharedAxisX.popExitTransition,
                ) {
                    composable(route = AppRoute.Onboarding.route) {
                        OnboardingScreen(
                            onComplete = {
                                navController.navigate(AppRoute.Home.route) {
                                    popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                                }
                            },
                        )
                    }
                    composable(route = AppRoute.Home.route) {
                        HomeScreen(
                            onOpenSmartSavingsClick = { navController.navigate(AppRoute.SmartSavings.route) },
                            onOpenGoalsClick = { navController.navigate(AppRoute.Goals.route) },
                            onOpenHistoryClick = { navController.navigate(AppRoute.History.route) },
                            onOpenLimitsClick = { navController.navigate(AppRoute.Limits.route) },
                            onOpenAchievementsClick = { navController.navigate(AppRoute.Achievements.route) },
                            onOpenNotificationsClick = { navController.navigate(AppRoute.Notifications.route) },
                            onOpenRecurringClick = { navController.navigate(AppRoute.Recurring.route) },
                            onOpenAccountsClick = { navController.navigate(AppRoute.Accounts.route) },
                            onCreateAssetClick = { navController.navigate(AppRoute.smartCreate(null)) },
                            onCreateGoalClick = { navController.navigate(AppRoute.goalCreate()) },
                            onGoalClick = { id -> navController.navigate(AppRoute.goalDetail(id)) },
                            onAddTransaction = { scan, recent ->
                                navController.navigate(AppRoute.addTransaction(scan = scan, recent = recent))
                            },
                            onEditTransaction = { id -> navController.navigate(AppRoute.addTransaction(editId = id)) },
                        )
                    }
                    composable(
                        route = AppRoute.Tools.route,
                        deepLinks = listOf(NavDeepLinks.tools)
                    ) {
                        ToolsScreen(
                            onCreditCalcClick = { navController.navigate(AppRoute.CreditCalculator.route) },
                            onDepositCalcClick = { navController.navigate(AppRoute.DepositCalculator.route) },
                            onGoalCalcClick = { navController.navigate(AppRoute.GoalCalculator.route) },
                            onSavingsAccountCalcClick = { navController.navigate(AppRoute.SavingsAccountCalculator.route) },
                            onMortgageCalcClick = { navController.navigate(AppRoute.MortgageCalculator.route) },
                            onEarlyRepayClick = { navController.navigate(AppRoute.EarlyRepayCalculator.route) },
                            onRentVsBuyClick = { navController.navigate(AppRoute.RentVsBuyCalculator.route) },
                            onSavedClick = { navController.navigate(AppRoute.SavedCalculations.route) },
                        )
                    }
                    composable(route = AppRoute.SavedCalculations.route) {
                        SavedCalculationsScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(
                        route = AppRoute.CreditCalculator.route,
                        deepLinks = listOf(NavDeepLinks.creditCalc)
                    ) {
                        CreditCalculatorScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(
                        route = AppRoute.SavingsAccountCalculator.route,
                        deepLinks = listOf(NavDeepLinks.savingsAccountCalc)
                    ) {
                        SavingsAccountCalculatorScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(
                        route = AppRoute.DepositCalculator.route,
                        deepLinks = listOf(NavDeepLinks.depositCalc)
                    ) {
                        DepositCalculatorScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(
                        route = AppRoute.GoalCalculator.route,
                        deepLinks = listOf(NavDeepLinks.goalCalc)
                    ) {
                        GoalCalculatorScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(
                        route = AppRoute.MortgageCalculator.route,
                        deepLinks = listOf(NavDeepLinks.mortgageCalc)
                    ) {
                        MortgageCalculatorScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(
                        route = AppRoute.EarlyRepayCalculator.route,
                        deepLinks = listOf(NavDeepLinks.earlyRepayCalc)
                    ) {
                        EarlyRepaymentCalculatorScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(
                        route = AppRoute.RentVsBuyCalculator.route,
                        deepLinks = listOf(NavDeepLinks.rentVsBuyCalc)
                    ) {
                        RentVsBuyScreen(onBack = { navController.popBackStackOrHome() })
                    }
                    composable(route = AppRoute.History.route) {
                        HistoryScreen(
                            onBack = { navController.popBackStackOrHome() },
                            onNavigateToAdd = { navController.navigate(AppRoute.addTransaction()) },
                            onEdit = { id -> navController.navigate(AppRoute.addTransaction(editId = id)) },
                        )
                    }
                    composable(route = AppRoute.SmartSavings.route) { backStackEntry ->
                        val snackbarMessage by backStackEntry.savedStateHandle
                            .getStateFlow<String?>(SmartSavingsSnackbar.KEY, null)
                            .collectAsStateWithLifecycle()
                        SmartSavingsScreen(
                            onBack = { navController.popBackStackOrHome() },
                            onCreateClick = { navController.navigate(AppRoute.smartCreate(null)) },
                            onOpenGoalsClick = { navController.navigate(AppRoute.Goals.route) },
                            onAssetClick = { id -> navController.navigate(AppRoute.smartDetail(id)) },
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
                    ) { createEntry ->
                        val assetIdStr = createEntry.arguments?.getString("assetId")
                        val assetId = assetIdStr?.toLongOrNull()
                        val successMessage =
                            if (assetId != null) stringResource(R.string.smart_updated_success)
                            else stringResource(R.string.smart_created_success)

                        CreateSmartSavingsScreen(
                            viewModel = koinViewModel(viewModelStoreOwner = createEntry) {
                                parametersOf(assetId)
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
                        arguments = listOf(navArgument("assetId") { type = NavType.LongType }),
                    ) { entry ->
                        val assetId = entry.arguments?.getLong("assetId") ?: 0L
                        val deletedMsg = stringResource(R.string.smart_deleted_success)
                        SmartSavingsDetailScreen(
                            assetId = assetId,
                            onBack = { navController.popBackStackOrHome() },
                            onEdit = { id -> navController.navigate(AppRoute.smartCreate(id)) },
                            onDeleteSuccess = {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(SmartSavingsSnackbar.KEY, deletedMsg)
                                navController.popBackStackOrHome()
                            },
                        )
                    }
                    composable(route = AppRoute.Goals.route) {
                        GoalsScreen(
                            onCreateClick = { navController.navigate(AppRoute.goalCreate()) },
                            onGoalClick = { id -> navController.navigate(AppRoute.goalDetail(id)) },
                            navController = navController
                        )
                    }
                    composable(route = AppRoute.Achievements.route) {
                        AchievementsScreen(
                            onBack = { navController.popBackStackOrHome() },
                            viewModel = achievementsViewModel,
                        )
                    }
                    composable(
                        route = AppRoute.GOAL_CREATE_WITH_ARGS,
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
                    composable(route = AppRoute.Analytics.route) {
                        AnalyticsScreen()
                    }
                    composable(route = AppRoute.Limits.route) {
                        LimitsScreen(navController = navController)
                    }
                    composable(route = AppRoute.Notifications.route) {
                        NotificationsScreen(navController = navController)
                    }
                    composable(route = AppRoute.Settings.route) {
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
                    composable(route = AppRoute.Permissions.route) {
                        PermissionsScreen(navController = navController)
                    }
                    composable(route = AppRoute.Export.route) {
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
                    composable(route = AppRoute.ImportPicker.route) {
                        ImportPickerDeepLinkScreen(navController = navController)
                    }
                    composable(route = AppRoute.Recurring.route) {
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
                    composable(route = AppRoute.Categories.route) {
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
                        arguments = listOf("quickCategory", "scan", "recent", "editId").map { name ->
                            navArgument(name) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        },
                        // Полноэкранная панель снизу вверх, 400ms cubic-bezier(.2,.8,.2,1) (§3.5, §6.2).
                        enterTransition = { slideInVertically(tween(400, easing = SheetEasing)) { it } },
                        exitTransition = { fadeOut(tween(150)) },
                        popEnterTransition = { fadeIn(tween(150)) },
                        popExitTransition = { slideOutVertically(tween(300, easing = SheetEasing)) { it } },
                    ) {
                        AddTransactionScreen(
                            onBack = { navController.popBackStackOrHome() },
                            onCreateCategory = { type ->
                                navController.navigate(AppRoute.categoryEdit(type = type.name))
                            },
                        )
                    }
                }
                MascotSnackbarHost(mascotSnackbar, Modifier.align(Alignment.TopCenter))
                val achievementText = pendingAchievement?.let {
                    stringResource(R.string.ach_unlocked_toast, it.title)
                }
                val haptics = LocalHapticFeedback.current
                LaunchedEffect(pendingAchievement?.key) {
                    if (achievementText != null) {
                        mascotSnackbar.show(achievementText, Kopi.TROPHY)
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        achievementsViewModel.clearPending()
                    }
                }
            }
        }
        }
    }
}
