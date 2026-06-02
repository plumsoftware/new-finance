package ru.plumsoftware.finance.presentation.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DonutSmall
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.StreakData
import ru.plumsoftware.finance.domain.model.daysLeft
import ru.plumsoftware.finance.domain.model.isOverdue
import ru.plumsoftware.finance.domain.model.progress
import ru.plumsoftware.finance.presentation.achievements.AchievementKeys
import ru.plumsoftware.finance.presentation.notifications.NotificationsViewModel
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.common.hasPendingPermissions
import ru.plumsoftware.finance.presentation.permissions.PermissionsBottomSheet
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.MascotImage
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.MascotEmotion
import ru.plumsoftware.finance.ui.theme.MascotSize
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onOpenSmartSavingsClick: () -> Unit = {},
    onOpenGoalsClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    onOpenAnalyticsClick: () -> Unit = {},
    onOpenLimitsClick: () -> Unit = {},
    onOpenAchievementsClick: () -> Unit = {},
    onOpenNotificationsClick: () -> Unit = {},
    onCreateAssetClick: () -> Unit = {},
    onCreateGoalClick: () -> Unit = {},
    onGoalClick: (Long) -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
    notificationsViewModel: NotificationsViewModel = koinViewModel(),
    settingsRepository: SettingsRepository = koinInject(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val unreadCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var permissionResumeTick by remember { mutableIntStateOf(0) }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    DisposableEffect(lifecycleOwner, settings.permissionsPromptHidden) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (!settings.permissionsPromptHidden && hasPendingPermissions(context)) {
                        showPermissionsSheet = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> permissionResumeTick++
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(settings.permissionsPromptHidden, permissionResumeTick) {
        if (settings.permissionsPromptHidden || !hasPendingPermissions(context)) {
            showPermissionsSheet = false
        } else if (hasPendingPermissions(context)) {
            showPermissionsSheet = true
        }
    }

    if (showPermissionsSheet) {
        PermissionsBottomSheet(
            onDismiss = { dontShowAgain ->
                showPermissionsSheet = false
                if (dontShowAgain) {
                    scope.launch {
                        settingsRepository.update { it.copy(permissionsPromptHidden = true) }
                    }
                }
            },
        )
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = Dimens.statusBarInset,
                bottom = Dimens.SpacingM,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
        ) {
            item {
                GreetingHeader(
                    unreadCount = unreadCount,
                    onNotificationsClick = onOpenNotificationsClick,
                )
            }
            item {
                BalanceCard(
                    balanceMinor = state.totalBalanceMinor,
                    monthIncomeMinor = state.monthIncomeMinor,
                    monthExpenseMinor = state.monthExpenseMinor,
                    currencyCode = state.currencyCode,
                    onMonthClick = onOpenAnalyticsClick,
                )
            }
            item {
                HomeQuickActionsRow(
                    onLimitsClick = onOpenLimitsClick,
                    onOperationsClick = onOpenHistoryClick,
                    onAchievementsClick = onOpenAchievementsClick,
                    hasBudgetWarnings = state.hasBudgetWarnings,
                    operationCount = state.recentTransactions.size,
                    unlockedAchievementsCount = state.unlockedAchievementsCount,
                    streak = state.streak,
                )
            }
            item {
                SectionWithAction(
                    sectionLabel = { SectionLabel(text = stringResource(R.string.goals_home_section)) },
                    actionLabel = stringResource(R.string.dashboard_see_all),
                    onActionClick = onOpenGoalsClick,
                )
            }
            item {
                state.featuredGoal?.let { featured ->
                    HomeFeaturedGoalCard(
                        goal = featured,
                        currencyCode = state.currencyCode,
                        onClick = { onGoalClick(featured.id) },
                    )
                } ?: HomeCreateGoalCard(onClick = onCreateGoalClick)
            }
            item {
                AnimatedVisibility(visible = state.hasBudgetWarnings) {
                    BudgetWarningBanner(
                        onClick = onOpenLimitsClick,
                        onDismiss = viewModel::dismissWarning,
                    )
                }
            }
            if (state.insights.isNotEmpty()) {
                item {
                    InsightsSection(
                        insights = state.insights,
                        categoryMap = state.categoryMap,
                        currencyCode = state.currencyCode,
                    )
                }
            }
            item {
                SectionWithAction(
                    sectionLabel = { SectionLabel(text = stringResource(R.string.smart_savings)) },
                    actionLabel = stringResource(R.string.dashboard_see_all),
                    onActionClick = onOpenSmartSavingsClick,
                )
            }
            item {
                if (state.smartAssets.isEmpty()) {
                    EmptyAssetCard(onClick = onCreateAssetClick)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = Dimens.SpacingL),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                    ) {
                        state.smartAssets.forEach { asset ->
                            AssetMiniCard(
                                asset = asset,
                                currencyCode = state.currencyCode,
                                onRecordUsage = { viewModel.recordSmartUsage(asset.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onOpenSmartSavingsClick: () -> Unit = {},
    onOpenGoalsClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    onOpenAnalyticsClick: () -> Unit = {},
    onOpenLimitsClick: () -> Unit = {},
    onOpenAchievementsClick: () -> Unit = {},
    onOpenNotificationsClick: () -> Unit = {},
    onCreateAssetClick: () -> Unit = {},
    onCreateGoalClick: () -> Unit = {},
    onGoalClick: (Long) -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
) {
    HomeScreen(
        onOpenSmartSavingsClick = onOpenSmartSavingsClick,
        onOpenGoalsClick = onOpenGoalsClick,
        onOpenHistoryClick = onOpenHistoryClick,
        onOpenAnalyticsClick = onOpenAnalyticsClick,
        onOpenLimitsClick = onOpenLimitsClick,
        onOpenAchievementsClick = onOpenAchievementsClick,
        onOpenNotificationsClick = onOpenNotificationsClick,
        onCreateAssetClick = onCreateAssetClick,
        onCreateGoalClick = onCreateGoalClick,
        onGoalClick = onGoalClick,
        viewModel = viewModel,
    )
}

@Composable
private fun GreetingHeader(
    unreadCount: Int,
    onNotificationsClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = greetingWithTime(),
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.dashboard_title),
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
        }
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            style = typography.labelSmall,
                        )
                    }
                }
            },
        ) {
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.cd_notifications),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(Dimens.IconSizeM),
                )
            }
        }
    }
}

@Composable
private fun HomeStreakInfo(
    streak: StreakData,
    modifier: Modifier = Modifier,
) {
    if (streak.currentStreak == 0) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
    ) {
        Text(
            text = "🔥 ${streak.currentStreak} ${streak.currentStreak.pluralDays()}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = when {
                streak.currentStreak >= 30 -> Color(0xFFFF9500)
                streak.currentStreak >= 7 -> Color(0xFFFF3B30)
                else -> MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = if (streak.todayHasActivity) {
                stringResource(R.string.streak_today_done)
            } else {
                stringResource(R.string.streak_today_pending)
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (streak.todayHasActivity) {
                Color(0xFF34C759)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            },
        )
    }
}

private fun Int.pluralDays(): String = when {
    this % 100 in 11..19 -> "дней"
    this % 10 == 1 -> "день"
    this % 10 in 2..4 -> "дня"
    else -> "дней"
}

@Composable
private fun greetingWithTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..11 -> stringResource(R.string.greeting_morning)
        hour in 12..17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
}

@Composable
private fun BalanceCard(
    balanceMinor: Long,
    monthIncomeMinor: Long,
    monthExpenseMinor: Long,
    currencyCode: String,
    onMonthClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val animatedBalance by animateFloatAsState(
        targetValue = balanceMinor.toFloat(),
        animationSpec = tween(durationMillis = 400),
        label = "home_balance",
    )
    val savingsRate = if (monthIncomeMinor > 0) {
        ((monthIncomeMinor - monthExpenseMinor).coerceAtLeast(0L).toFloat() / monthIncomeMinor)
            .coerceIn(0f, 1f)
    } else {
        0f
    }
    val savingsPercent = (savingsRate * 100).roundToInt()
    val monthLabel = SimpleDateFormat("LLLL yyyy", Locale.forLanguageTag("ru"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }

    val balanceCardScale = 0.855f
    val balancePadding = Dimens.SpacingL * balanceCardScale
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
    ) {
        Column(
            modifier = Modifier.padding(balancePadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS * balanceCardScale),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.balance_label),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                MonthSelector(
                    monthLabel = monthLabel,
                    onClick = onMonthClick,
                )
            }
            Text(
                text = MoneyFormat.format(animatedBalance.roundToInt().toLong(), currencyCode),
                style = typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HorizontalDivider(
                color = colors.surfaceVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = Dimens.SpacingS * balanceCardScale),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FinanceStat(
                    icon = Icons.Rounded.ArrowUpward,
                    tint = colors.secondary,
                    label = stringResource(R.string.income),
                    value = MoneyFormat.format(monthIncomeMinor, currencyCode, showSign = true),
                )
                FinanceStat(
                    icon = Icons.Rounded.ArrowDownward,
                    tint = colors.error,
                    label = stringResource(R.string.expenses),
                    value = MoneyFormat.format(monthExpenseMinor, currencyCode),
                )
            }
            if (savingsRate > 0f) {
                LinearProgressIndicator(
                    progress = { savingsRate },
                    color = colors.secondary,
                    trackColor = colors.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(Dimens.RadiusPill)),
                )
                Text(
                    text = stringResource(R.string.savings_rate_saved, savingsPercent),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HomeQuickActionsRow(
    onLimitsClick: () -> Unit,
    onOperationsClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    hasBudgetWarnings: Boolean,
    operationCount: Int,
    unlockedAchievementsCount: Int,
    streak: StreakData,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(horizontal = Dimens.SpacingL),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
    ) {
        HomeAchievementsQuickCard(
            streak = streak,
            unlockedCount = unlockedAchievementsCount,
            onClick = onAchievementsClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            HomeQuickActionCard(
                title = stringResource(R.string.home_all_operations),
                subtitle = stringResource(R.string.home_operations_count, operationCount),
                icon = Icons.Outlined.History,
                onClick = onOperationsClick,
            )
            HomeLimitsQuickCard(
                hasBudgetWarnings = hasBudgetWarnings,
                onClick = onLimitsClick,
            )
        }
    }
}

@Composable
private fun HomeLimitsQuickCard(
    hasBudgetWarnings: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
        ) {
            Icon(
                imageVector = Icons.Rounded.DonutSmall,
                contentDescription = null,
                tint = if (hasBudgetWarnings) colors.error else colors.primary,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
                Text(
                    text = stringResource(R.string.limits),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (hasBudgetWarnings) {
                        stringResource(R.string.budget_warning_title)
                    } else {
                        stringResource(R.string.current_month_limits)
                    },
                    style = typography.bodySmall,
                    color = if (hasBudgetWarnings) colors.error else colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeAchievementsQuickCard(
    streak: StreakData,
    unlockedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val streakBackground = when {
        streak.currentStreak >= 30 -> Color(0xFFFF9500).copy(alpha = 0.12f)
        streak.currentStreak >= 7 -> Color(0xFFFF3B30).copy(alpha = 0.10f)
        streak.currentStreak > 0 -> colors.surface
        else -> Color.Transparent
    }
    AppCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(Dimens.RadiusL))
                .background(streakBackground)
                .padding(Dimens.SpacingM),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            MascotImage(
                emotion = MascotEmotion.HAPPY,
                modifier = Modifier.size(MascotSize.Medium),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
            ) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (streak.currentStreak > 0) {
                    HomeStreakInfo(streak = streak)
                } else {
                    Text(
                        text = stringResource(
                            R.string.home_achievements_count,
                            unlockedCount,
                            AchievementKeys.TOTAL_COUNT,
                        ),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Text(
                text = title,
                style = typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MonthSelector(
    monthLabel: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
    ) {
        Text(
            text = monthLabel,
            style = typography.bodyMedium,
            color = colors.primary,
        )
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(Dimens.IconSizeS),
        )
    }
}

@Composable
private fun FinanceStat(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(Dimens.IconSizeS),
            )
            Text(
                text = value,
                style = typography.bodyMedium,
                color = tint,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = label,
            style = typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun BudgetWarningBanner(
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL)
            .clickable(onClick = onClick),
        containerColor = colors.error.copy(alpha = 0.08f),
        borderColor = colors.error.copy(alpha = 0.3f),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = colors.error,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.SpacingS),
            ) {
                Text(
                    text = stringResource(R.string.budget_warning_title),
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.error,
                )
                Text(
                    text = stringResource(R.string.budget_warning_body),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    tint = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionWithAction(
    sectionLabel: @Composable () -> Unit,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = Dimens.SpacingS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        sectionLabel()
        IosTextButton(
            text = actionLabel,
            onClick = onActionClick,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyAssetCard(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    val cornerRadius = Dimens.RadiusL

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL)
            .drawBehind {
                drawRoundRect(
                    color = colors.primary.copy(alpha = 0.45f),
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect),
                )
            },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(Dimens.IconSizeM),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs)) {
                Text(
                    text = stringResource(R.string.add_first_asset),
                    style = typography.bodyLarge,
                    color = colors.primary,
                )
                Text(
                    text = stringResource(R.string.track_roi),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HomeCreateGoalCard(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.primary.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = colors.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.goals_add_button),
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.goals_empty_subtitle),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.outlineVariant,
                modifier = Modifier.size(Dimens.IconSizeS),
            )
        }
    }
}

@Composable
private fun HomeFeaturedGoalCard(
    goal: Goal,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val leftDays = goal.daysLeft
    val deadlineText = when {
        leftDays == null -> stringResource(R.string.goal_no_deadline)
        goal.isOverdue -> stringResource(R.string.goal_days_overdue, kotlin.math.abs(leftDays))
        else -> stringResource(R.string.goal_days_left, leftDays)
    }
    val accent = if (goal.isCompleted) IosGreen else colors.primary
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(colors.primary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = goal.emoji, style = typography.titleLarge)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Dimens.SpacingS),
                ) {
                    Text(
                        text = goal.name,
                        style = typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.goal_saved_of,
                            MoneyFormat.format(goal.savedAmountMinor, currencyCode),
                            MoneyFormat.format(goal.targetAmountMinor, currencyCode),
                        ),
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.progressHeightThin)
                    .clip(RoundedCornerShape(Dimens.RadiusPill)),
                color = accent,
                trackColor = colors.outline,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.percent_short, (goal.progress * 100).roundToInt()),
                    style = typography.labelSmall,
                    color = accent,
                )
                Text(
                    text = deadlineText,
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AssetMiniCard(
    asset: SmartAsset,
    currencyCode: String,
    onRecordUsage: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val progress = asset.paybackProgress.coerceIn(0f, 1f)
    val progressColor = if (asset.status == SmartAssetStatus.PROFIT) {
        colors.secondary
    } else {
        colors.primary
    }

    AppCard(
        modifier = Modifier.width(Dimens.smartCardWidth),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(progressColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = asset.icon, style = typography.titleMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        style = typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.percent_short, (progress * 100).roundToInt()),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.progressHeightThin)
                    .clip(RoundedCornerShape(Dimens.RadiusPill)),
                color = progressColor,
                trackColor = colors.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = stringResource(
                    R.string.dashboard_saved_amount,
                    MoneyFormat.format(asset.totalSavedMinor, currencyCode),
                ),
                style = typography.bodySmall,
                color = colors.secondary,
            )
            OutlinedButton(
                onClick = onRecordUsage,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.RadiusS),
                border = BorderStroke(Dimens.borderThin, colors.primary),
                contentPadding = PaddingValues(
                    horizontal = Dimens.SpacingS,
                    vertical = Dimens.SpacingS,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.dashboard_record),
                    style = typography.labelMedium,
                )
            }
        }
    }
}

