package ru.plumsoftware.finance.presentation.dashboard

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.achievements.AchievementId
import ru.plumsoftware.finance.domain.budget.BudgetMath
import ru.plumsoftware.finance.domain.budget.DailyBudget
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.progress
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.common.TxItem
import ru.plumsoftware.finance.presentation.common.hasPendingPermissions
import ru.plumsoftware.finance.presentation.permissions.PermissionsBottomSheet
import ru.plumsoftware.finance.ui.ads.NativeAdContainer
import ru.plumsoftware.finance.ui.ads.NativeAdSession
import ru.plumsoftware.finance.ui.ds.BlockGap
import ru.plumsoftware.finance.ui.ds.ButtonTonal
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.DashedCard
import ru.plumsoftware.finance.ui.ds.DayState
import ru.plumsoftware.finance.ui.ds.DayStrip
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FProgressBar
import ru.plumsoftware.finance.ui.ds.FitText
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.IconButton44
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.KopiImage
import ru.plumsoftware.finance.ui.ds.ListRow
import ru.plumsoftware.finance.ui.ds.MascotTip
import ru.plumsoftware.finance.ui.ds.ProgressRing
import ru.plumsoftware.finance.ui.ds.RootBottomInset
import ru.plumsoftware.finance.ui.ds.ScreenPadding
import ru.plumsoftware.finance.ui.ds.SectionTitle
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.ds.masked
import ru.plumsoftware.finance.ui.ds.LocalAmountVisibility
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.LocalTime

@Composable
fun HomeScreen(
    onOpenSmartSavingsClick: () -> Unit = {},
    onOpenGoalsClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    onOpenLimitsClick: () -> Unit = {},
    onOpenAchievementsClick: () -> Unit = {},
    onOpenNotificationsClick: () -> Unit = {},
    onOpenRecurringClick: () -> Unit = {},
    onOpenAccountsClick: () -> Unit = {},
    onCreateAssetClick: () -> Unit = {},
    onCreateGoalClick: () -> Unit = {},
    onGoalClick: (Long) -> Unit = {},
    onAddTransaction: (scan: Boolean, recent: Boolean) -> Unit = { _, _ -> },
    onEditTransaction: (Long) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
    settingsRepository: SettingsRepository = koinInject(),
    accountRepository: AccountRepository = koinInject(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val c = FinanceTheme.colors

    // region Первый запуск: разрешения и начальный баланс (§6.14)
    val lifecycleOwner = LocalLifecycleOwner.current
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var showInitialBalance by remember { mutableStateOf(false) }
    var resumeTick by remember { mutableIntStateOf(0) }
    var settingsLoaded by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) resumeTick++ }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        settingsRepository.settings.first()
        settingsLoaded = true
    }
    LaunchedEffect(settingsLoaded, settings.permissionsPromptHidden, resumeTick) {
        if (!settingsLoaded) return@LaunchedEffect
        showPermissionsSheet = !settings.permissionsPromptHidden && hasPendingPermissions(context)
    }
    LaunchedEffect(settingsLoaded, settings.initialBalancePromptCompleted, state.isLoading, showPermissionsSheet) {
        if (!settingsLoaded || state.isLoading || settings.initialBalancePromptCompleted) {
            showInitialBalance = false
            return@LaunchedEffect
        }
        if (showPermissionsSheet || hasPendingPermissions(context) && !settings.permissionsPromptHidden) return@LaunchedEffect
        val account = accountRepository.getDefault() ?: return@LaunchedEffect
        if (account.initialBalanceMinor != 0L || state.totalBalanceMinor != 0L) {
            settingsRepository.update { it.copy(initialBalancePromptCompleted = true) }
            return@LaunchedEffect
        }
        showInitialBalance = true
    }
    if (showPermissionsSheet) {
        PermissionsBottomSheet(onDismiss = { dontShowAgain ->
            showPermissionsSheet = false
            if (dontShowAgain) scope.launch { settingsRepository.update { it.copy(permissionsPromptHidden = true) } }
        })
    }
    if (showInitialBalance) {
        AmountEntrySheet(
            title = stringResource(R.string.initial_balance_prompt_title),
            message = stringResource(R.string.initial_balance_prompt_message),
            confirmLabel = stringResource(R.string.save),
            dismissLabel = stringResource(R.string.initial_balance_prompt_skip),
            currencyCode = state.currencyCode,
            onConfirm = { minor ->
                scope.launch {
                    accountRepository.getDefault()?.let { accountRepository.upsert(it.copy(initialBalanceMinor = minor)) }
                    settingsRepository.update { it.copy(initialBalancePromptCompleted = true) }
                    showInitialBalance = false
                }
            },
            onDismiss = {
                scope.launch {
                    settingsRepository.update { it.copy(initialBalancePromptCompleted = true) }
                    showInitialBalance = false
                }
            },
        )
    }
    // endregion

    val shareText = stringResource(R.string.home_share_app_text, AppConfig.storeListingUrl)
    val shareTitle = stringResource(R.string.home_share_app)

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = RootBottomInset),
        verticalArrangement = Arrangement.spacedBy(BlockGap),
    ) {
        item(key = "header") {
            HomeHeader(
                streak = state.streak,
                hasUnread = state.unreadNotifications > 0,
                onStreak = onOpenAchievementsClick,
                onShare = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(send, shareTitle))
                },
                onBell = onOpenNotificationsClick,
            )
        }
        item(key = "hero") {
            BalanceHero(
                balance = state.totalBalanceMinor,
                monthlyChange = state.monthlyNetMinor,
                currency = state.currencyCode,
                onOpen = onOpenAccountsClick,
            )
        }
        item(key = "monthly-summary") {
            MonthlyIncomeExpenseRow(
                income = state.monthlyIncomeMinor,
                expenses = state.monthlyExpenseMinor,
                currency = state.currencyCode,
            )
        }
        item(key = "quick") {
            QuickActions(
                achievementsUnlocked = state.achievementsUnlocked,
                onScan = { onAddTransaction(true, false) },
                onRepeat = { onAddTransaction(false, true) },
                onLimits = onOpenLimitsClick,
                onAchievements = onOpenAchievementsClick,
            )
        }
        if (AppConfig.nativeHome.isNotBlank() && !NativeAdSession.isDismissed(AppConfig.nativeHome)) {
            item(key = "ad") {
                FCard(padding = PaddingValues(0.dp)) { NativeAdContainer(adUnitId = AppConfig.nativeHome) }
            }
        }
        state.tip?.let { tip ->
            item(key = "tip") {
                SwipeableTip(text = kopiTipText(tip, state.currencyCode), onDismiss = viewModel::hideTipForToday)
            }
        }
        if (state.upcoming.isNotEmpty()) {
            item(key = "upcoming") { UpcomingCard(state, onMore = onOpenRecurringClick) }
        }
        item(key = "goals") {
            GoalsStrip(state.goals, state.currencyCode, onAll = onOpenGoalsClick, onGoal = onGoalClick, onCreate = onCreateGoalClick)
        }
        item(key = "smart") {
            SmartSavingsRow(state.smart, state.currencyCode, onOpen = onOpenSmartSavingsClick, onCreate = onCreateAssetClick)
        }
        item(key = "today") {
            TodayCard(
                ops = state.todayOps,
                onAll = onOpenHistoryClick,
                onAdd = { onAddTransaction(false, false) },
                onOpen = onEditTransaction,
            )
        }
        item(key = "accounts") {
            AccountsRow(
                count = state.accountsCount,
                balance = state.totalBalanceMinor,
                currency = state.currencyCode,
                onOpen = onOpenAccountsClick,
            )
        }
    }
}

@Composable
private fun greeting(): String {
    val h = LocalTime.now().hour
    return stringResource(
        when (h) {
            in 5..11 -> R.string.home_greeting_morning
            in 12..17 -> R.string.home_greeting_day
            in 18..23 -> R.string.home_greeting_evening
            else -> R.string.home_greeting_night
        },
    )
}

@Composable
private fun HomeHeader(streak: Int, hasUnread: Boolean, onStreak: () -> Unit, onShare: () -> Unit, onBell: () -> Unit) {
    val c = FinanceTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(greeting(), style = FinanceType.bodySmall, color = c.textSecondary)
            Text(stringResource(R.string.nav_home), style = FinanceType.headline, color = c.textPrimary)
        }
        Row(
            Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(c.streakBg)
                .clickable(role = Role.Button, onClick = onStreak)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔥 $streak", style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Bold), color = c.streakText)
        }
        HSpace(4.dp)
        IconButton44(R.drawable.ic_share, stringResource(R.string.home_share_app), onShare, size = 40.dp)
        IconButton44(R.drawable.ic_bell, stringResource(R.string.notifications), onBell, size = 40.dp, badge = hasUnread)
    }
}

// Карточка общего баланса на главном экране.
@Composable
private fun BalanceHero(
    balance: Long,
    monthlyChange: Long,
    currency: String,
    onOpen: () -> Unit,
) {
    val c = FinanceTheme.colors
    val primary = c.primary
    // Второй оттенок выводится из primary темы, а не задаётся отдельным цветом.
    val lighterPrimary = Color(
        red = primary.red + (1f - primary.red) * 0.12f,
        green = primary.green + (1f - primary.green) * 0.12f,
        blue = primary.blue + (1f - primary.blue) * 0.12f,
        alpha = primary.alpha,
    )
    val visibility = LocalAmountVisibility.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(primary, lighterPrimary)))
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(start = 20.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            val balanceText = masked(Money.formatRounded(balance, currency))
            // Подбираем размер по доступной ширине и длине форматированной суммы.
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val baseSize = FinanceType.displayHero.fontSize.value
                val estimatedTextWidth = balanceText.length * baseSize * 0.57f
                val fittedSize = if (estimatedTextWidth > maxWidth.value) {
                    (baseSize * maxWidth.value / estimatedTextWidth).coerceAtLeast(18f)
                } else baseSize
                Text(
                    text = balanceText,
                    modifier = Modifier.clickable(
                        enabled = visibility.hidden,
                        onClickLabel = stringResource(R.string.home_show),
                        onClick = visibility.toggle,
                    ),
                    style = FinanceType.displayHero.copy(fontSize = fittedSize.sp),
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                )
            }
            VSpace(5.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (monthlyChange >= 0L) "▲" else "▼",
                    style = FinanceType.caption.copy(fontWeight = FontWeight.Bold),
                    color = if (monthlyChange >= 0L) c.onInkSuccess else Color.White.copy(alpha = 0.9f),
                )
                HSpace(4.dp)
                Text(
                    text = buildString {
                        append(if (monthlyChange > 0L) "+" else if (monthlyChange < 0L) "−" else "")
                        append(Money.formatRounded(kotlin.math.abs(monthlyChange), currency))
                        append(" за месяц")
                    },
                    style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (monthlyChange >= 0L) c.onInkSuccess else Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Box(
            modifier = Modifier
                .width(112.dp)
                .height(76.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 2.dp, end = 1.dp, top = 4.dp),
            ) {
                val points = listOf(
                    0.02f to 0.88f,
                    0.16f to 0.78f,
                    0.29f to 0.58f,
                    0.42f to 0.34f,
                    0.55f to 0.43f,
                    0.68f to 0.28f,
                    0.82f to 0.20f,
                    0.98f to 0.06f,
                )
                val path = Path().apply {
                    points.forEachIndexed { index, point ->
                        val x = size.width * point.first
                        val y = size.height * point.second
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.78f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
                val last = points.last()
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 2.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(size.width * last.first, size.height * last.second),
                )
            }

        }
    }
}

@Composable
private fun MonthlyIncomeExpenseRow(income: Long, expenses: Long, currency: String) {
    val c = FinanceTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MonthlySummaryCard(
            title = "Доходы",
            amount = income,
            currency = currency,
            symbol = "↓",
            symbolColor = c.successText,
            symbolBackground = c.successText.copy(alpha = 0.13f),
            modifier = Modifier.weight(1f),
        )
        MonthlySummaryCard(
            title = "Расходы",
            amount = expenses,
            currency = currency,
            symbol = "↑",
            symbolColor = c.primary,
            symbolBackground = c.primary.copy(alpha = 0.13f),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MonthlySummaryCard(
    title: String,
    amount: Long,
    currency: String,
    symbol: String,
    symbolColor: Color,
    symbolBackground: Color,
    modifier: Modifier = Modifier,
) {
    val c = FinanceTheme.colors
    Column(
        modifier = modifier
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(c.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(symbolBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(symbol, color = symbolColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            HSpace(7.dp)
            Text(
                text = title,
                style = FinanceType.caption,
                color = c.textSecondary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
        VSpace(6.dp)
        val amountText = masked(Money.formatRounded(amount, currency))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val baseSize = FinanceType.body.fontSize.value
            val estimatedTextWidth = amountText.length * baseSize * 0.56f
            val fittedSize = if (estimatedTextWidth > maxWidth.value) {
                (baseSize * maxWidth.value / estimatedTextWidth).coerceAtLeast(11f)
            } else baseSize
            Text(
                text = amountText,
                style = FinanceType.body.copy(fontWeight = FontWeight.Bold, fontSize = fittedSize.sp),
                color = c.textPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun QuickActions(
    achievementsUnlocked: Int,
    onScan: () -> Unit,
    onRepeat: () -> Unit,
    onLimits: () -> Unit,
    onAchievements: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickTile(R.drawable.ic_quick_receipt, stringResource(R.string.home_quick_scan), onScan, Modifier.weight(1f))
        QuickTile(R.drawable.ic_quick_repeat, stringResource(R.string.home_quick_repeat), onRepeat, Modifier.weight(1f))
        QuickTile(R.drawable.ic_quick_limits, stringResource(R.string.limits), onLimits, Modifier.weight(1f))
        QuickTile(
            R.drawable.ic_quick_awards,
            stringResource(R.string.home_quick_achievements, achievementsUnlocked, AchievementId.entries.size),
            onAchievements,
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickTile(iconRes: Int, label: String, onClick: () -> Unit, modifier: Modifier) {
    val c = FinanceTheme.colors
    Column(
        modifier
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(c.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = c.primary,
            modifier = Modifier.size(22.dp),
        )
        VSpace(4.dp)
        FitText(label, style = FinanceType.caption.copy(fontWeight = FontWeight.Medium), color = c.textPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTip(text: String, onDismiss: () -> Unit) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissBoxValue.Settled) onDismiss()
    }
    SwipeToDismissBox(state = state, backgroundContent = {}) {
        MascotTip(text = text)
    }
}

// region «Скоро спишется» (§6.1.5)

@Composable
private fun UpcomingCard(state: HomeUiState, onMore: () -> Unit) {
    val c = FinanceTheme.colors
    FCard {
        SectionTitle(
            text = stringResource(R.string.home_upcoming_title),
            trailing = {
                Text(masked(Money.formatRounded(state.upcomingSum, state.currencyCode)), style = FinanceType.title, color = c.textPrimary)
            },
        )
        VSpace(4.dp)
        state.upcoming.forEachIndexed { i, u ->
            if (i > 0) CardDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.width(38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(u.charge.date.dayOfMonth.toString(), style = FinanceType.titleSection, color = c.textPrimary)
                    Text(DateFmt.monthShort(u.charge.date), style = FinanceType.micro, color = c.textSecondary)
                }
                HSpace(10.dp)
                Box(
                    Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(if (u.charge.daysUntil <= 3) c.warning else c.outline, RoundedCornerShape(2.dp)),
                )
                HSpace(12.dp)
                Text(u.emoji, fontSize = 20.sp)
                HSpace(10.dp)
                Column(Modifier.weight(1f)) {
                    Text(u.charge.title, style = FinanceType.bodyMedium, color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(whenLabel(u.charge.daysUntil), style = FinanceType.caption, color = c.textSecondary)
                }
                Text(masked(Money.formatRounded(u.charge.amountMinor, state.currencyCode)), style = FinanceType.body.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
            }
        }
        if (state.upcomingMore > 0) {
            CardDivider()
            Text(
                stringResource(R.string.home_upcoming_more, state.upcomingMore),
                style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = c.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onMore)
                    .padding(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun whenLabel(days: Int): String = when (days) {
    0 -> stringResource(R.string.home_today_lower)
    1 -> stringResource(R.string.home_tomorrow)
    else -> pluralStringResource(R.plurals.pl_in_days, days, days)
}

// endregion

// region Цели (§6.1.6)

@Composable
private fun GoalsStrip(goals: List<Goal>, currency: String, onAll: () -> Unit, onGoal: (Long) -> Unit, onCreate: () -> Unit) {
    Column {
        SectionTitle(stringResource(R.string.goals_title), action = stringResource(R.string.home_all), onAction = onAll)
        VSpace(8.dp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(goals, key = { it.id }) { g -> GoalCard(g, currency) { onGoal(g.id) } }
            item(key = "new") {
                DashedCard(
                    modifier = Modifier
                        .width(130.dp)
                        .height(172.dp),
                    onClick = onCreate,
                ) {
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("+", style = FinanceType.headline, color = FinanceTheme.colors.primary)
                        Text(
                            stringResource(R.string.home_new_goal),
                            style = FinanceType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FinanceTheme.colors.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(g: Goal, currency: String, onClick: () -> Unit) {
    val c = FinanceTheme.colors
    FCard(modifier = Modifier.width(200.dp).height(172.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(progress = g.progress, size = 52.dp, stroke = 5.dp) { Text(g.emoji, fontSize = 20.sp) }
            HSpace(12.dp)
            Text(
                "${(g.progress * 100).toInt()}%",
                style = FinanceType.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
                color = c.primary,
            )
        }
        VSpace(10.dp)
        Text(g.name, style = FinanceType.title, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            stringResource(R.string.home_goal_saved, masked(Money.formatRounded(g.savedAmountMinor, g.currencyCode))),
            style = FinanceType.caption,
            color = c.textSecondary,
            maxLines = 1,
        )
        Text(
            stringResource(R.string.home_goal_of, masked(Money.formatRounded(g.targetAmountMinor, g.currencyCode))),
            style = FinanceType.caption,
            color = c.textSecondary,
            maxLines = 1,
        )
    }
}

// endregion

@Composable
private fun SmartSavingsRow(s: SmartSummary, currency: String, onOpen: () -> Unit, onCreate: () -> Unit) {
    val c = FinanceTheme.colors
    if (s.assets == 0) {
        DashedCard(modifier = Modifier.fillMaxWidth(), onClick = onCreate) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge("🌱", c.success, size = 44.dp, circle = false)
                HSpace(12.dp)
                Column {
                    Text(stringResource(R.string.home_smart_first), style = FinanceType.title, color = c.textPrimary)
                    Text(stringResource(R.string.home_smart_first_sub), style = FinanceType.caption, color = c.textSecondary)
                }
            }
        }
        return
    }
    FCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge("🌱", c.success, size = 44.dp, circle = false)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.smart_savings_title), style = FinanceType.title, color = c.textPrimary)
                Text(
                    "${pluralStringResource(R.plurals.pl_assets, s.assets, s.assets)} · ${pluralStringResource(R.plurals.pl_uses, s.uses, s.uses)}",
                    style = FinanceType.caption,
                    color = c.textSecondary,
                )
            }
            Text(masked(Money.withSign(s.savedMinor, currency)), style = FinanceType.title.copy(fontWeight = FontWeight.Bold), color = c.successText)
        }
    }
}

// region «Сегодня» (§6.1.9)

@Composable
private fun TodayCard(ops: List<TxItem>, onAll: () -> Unit, onAdd: () -> Unit, onOpen: (Long) -> Unit) {
    val c = FinanceTheme.colors
    FCard {
        SectionTitle(stringResource(R.string.home_today), action = stringResource(R.string.home_all_history), onAction = onAll)
        if (ops.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                KopiImage(Kopi.THINKING, 72.dp)
                VSpace(8.dp)
                Text(stringResource(R.string.home_today_empty), style = FinanceType.bodySmall, color = c.textSecondary)
                VSpace(10.dp)
                ButtonTonal(stringResource(R.string.home_add), onAdd, compact = true)
            }
        } else {
            ops.forEachIndexed { i, op ->
                if (i > 0) CardDivider()
                TxRow(op, onClick = { onOpen(op.id) })
            }
        }
    }
}

/** Строка операции (§6.1.9): иконка-круг 42dp, название, «Категория · 13:15», сумма. */
@Composable
fun TxRow(op: TxItem, onClick: (() -> Unit)? = null, showAccount: Boolean = false) {
    val c = FinanceTheme.colors
    val meta = buildList {
        add(op.categoryName)
        if (showAccount && op.accountName != null) add(op.accountName)
        add(op.time)
    }.distinct().joinToString(" · ")
    ListRow(
        title = op.title,
        subtitle = meta,
        value = masked(Money.signed(op.amountMinor, op.isIncome, op.currencyCode)),
        valueColor = if (op.isIncome) c.successText else c.textPrimary,
        leading = { EmojiBadge(op.emoji, op.color, size = 42.dp) },
        onClick = onClick,
    )
}

// endregion

@Composable
private fun AccountsRow(count: Int, balance: Long, currency: String, onOpen: () -> Unit) {
    val c = FinanceTheme.colors
    val visibility = LocalAmountVisibility.current
    val hidden = visibility.hidden
    val onToggle = visibility.toggle
    FCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge("💳", c.primary, size = 42.dp)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_all_accounts, count), style = FinanceType.caption, color = c.textSecondary)
                Text(
                    masked(Money.format(balance, currency, forceFraction = true)),
                    style = FinanceType.title.copy(fontWeight = FontWeight.Bold),
                    color = c.textPrimary,
                )
            }
            Text(
                stringResource(if (hidden) R.string.home_show else R.string.home_hide),
                style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = c.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onToggle)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
            )
        }
    }
}
