package ru.plumsoftware.finance.presentation.analytics

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.*
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.common.StatsPeriod
import ru.plumsoftware.finance.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Нативные цвета iOS
val IosBlue = Color(0xFF007AFF)
val IosGreen = Color(0xFF34C759)
val IosRed = Color(0xFFFF3B30)
val IosOrange = Color(0xFFFF9500)
val IosPurple = Color(0xFF5856D6)
val IosViolet = Color(0x1A5856D6)

private val periodTabs = listOf(
    StatsPeriod.DAY,
    StatsPeriod.WEEK,
    StatsPeriod.MONTH,
    StatsPeriod.YEAR,
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = koinInject(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var showDateRangePicker by remember { mutableStateOf(false) }

    val dateRangeActive = state.period == StatsPeriod.CUSTOM &&
            state.customStartMillis != null &&
            state.customEndMillis != null

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = state.customStartMillis,
        initialSelectedEndDateMillis = state.customEndMillis,
        initialDisplayMode = DisplayMode.Picker,
    )

    // ── Выбор диапазона дат (BottomSheet) ──────────────────
    if (showDateRangePicker || state.showDateRangePicker) {
        ModalBottomSheet(
            onDismissRequest = {
                showDateRangePicker = false
                viewModel.dismissDateRangePicker()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(width = 36.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(colors.onSurfaceVariant.copy(alpha = 0.15f)),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.pick_date_range),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        showDateRangePicker = false
                        viewModel.dismissDateRangePicker()
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                }

                DateRangePicker(
                    state = dateRangePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.surface,
                        selectedDayContainerColor = IosBlue,
                        selectedDayContentColor = Color.White,
                        dayInSelectionRangeContainerColor = IosBlue.copy(alpha = 0.1f),
                        dayInSelectionRangeContentColor = IosBlue,
                        todayContentColor = IosBlue,
                        todayDateBorderColor = IosBlue,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            showDateRangePicker = false
                            viewModel.dismissDateRangePicker()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.outlineVariant),
                    ) {
                        Text(stringResource(R.string.action_cancel), color = colors.onSurface)
                    }
                    Button(
                        onClick = {
                            val startMs = dateRangePickerState.selectedStartDateMillis
                            val endMs = dateRangePickerState.selectedEndDateMillis
                            if (startMs != null && endMs != null) {
                                viewModel.applyCustomRange(startMs, endMs)
                            }
                            showDateRangePicker = false
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null &&
                                dateRangePickerState.selectedEndDateMillis != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosBlue,
                            disabledContainerColor = IosBlue.copy(alpha = 0.3f),
                        ),
                    ) {
                        Text(stringResource(R.string.action_apply), color = Color.White)
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshCurrentPeriod()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val incomeTotal = state.summary.incomeMinor
    val expenseTotal = state.summary.expenseMinor
    val netTotal = state.summary.netMinor
    val savingsRate = if (incomeTotal > 0L) {
        ((incomeTotal - expenseTotal).coerceAtLeast(0L).toFloat() / incomeTotal).coerceIn(0f, 1f)
    } else 0f
    val savingsPercent = (savingsRate * 100).roundToInt()
    val expenseSegments = categoryDonutSegments(state.expenseCategories, colors)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
    ) { _ ->
        // ── Контент экрана ─────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Dimens.SpacingXs),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .padding(top = Dimens.SpacingXxl),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.analytics),
                            style = typography.titleLarge.copy(fontSize = 28.sp),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = if (dateRangeActive) IosBlue else colors.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { showDateRangePicker = true },
                        )
                    }

                    // Переключатель периодов без обрезки текста
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PeriodTabRow(
                            selectedPeriod = state.period,
                            onSelect = { viewModel.selectPeriod(it) },
                            enabled = !dateRangeActive,
                            modifier = Modifier
                                .weight(1.3f)
                                .alpha(if (dateRangeActive) 0.38f else 1f),
                        )
                        Spacer(Modifier.width(12.dp))
                        AnimatedVisibility(visible = !dateRangeActive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    onClick = viewModel::navigatePeriodBack,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronLeft,
                                        contentDescription = null,
                                        tint = colors.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = state.periodLabel.orEmpty(),
                                    style = typography.labelMedium,
                                    color = colors.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 90.dp),
                                )
                                IconButton(
                                    onClick = viewModel::navigatePeriodForward,
                                    enabled = state.canNavigateForward,
                                    modifier = Modifier.size(32.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = if (state.canNavigateForward) colors.onSurfaceVariant else colors.outlineVariant,
                                    )
                                }
                            }
                        }
                    }

                    // Чип для выбранного кастомного диапазона
                    AnimatedVisibility(
                        visible = dateRangeActive,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        val startLabel =
                            state.customStartMillis?.let { formatEpochMillis(it, "d MMM") }
                                .orEmpty()
                        val endLabel =
                            state.customEndMillis?.let { formatEpochMillis(it, "d MMM yyyy") }
                                .orEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(IosBlue.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DateRange,
                                contentDescription = null,
                                tint = IosBlue,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(
                                    R.string.date_range_label,
                                    startLabel,
                                    endLabel
                                ),
                                style = typography.bodySmall,
                                color = IosBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(
                                onClick = { viewModel.selectPeriod(StatsPeriod.MONTH) },
                                modifier = Modifier.size(20.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = IosBlue,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = colors.onSurface.copy(alpha = 0.06f))
                }
            }
            // Интеллектуальный совет от Коппи
            item {
                AnalyticsInsightCard(
                    expenseTotal = expenseTotal,
                    incomeTotal = incomeTotal,
                    savingsPercent = savingsPercent,
                )
            }

            // Сводный баланс за период
            item {
                SummaryCard(
                    netTotal = netTotal,
                    incomeTotal = incomeTotal,
                    expenseTotal = expenseTotal,
                    savingsPercent = savingsPercent,
                    savingsRate = savingsRate,
                    currencyCode = state.currencyCode,
                )
            }

            // Аналитика по счетам
            item {
                AccountsAnalyticsSection(
                    accounts = state.accountAnalytics,
                    currencyCode = state.currencyCode,
                    isExpanded = state.isAccountsSectionExpanded,
                    onToggleExpand = viewModel::toggleAccountsSection,
                )
            }

            // Расходы по дням (График)
            item {
                SectionHeader(text = stringResource(R.string.expenses_by_day))
                AppCard {
                    ExpenseColumnChart(dailyBars = state.dailyBars)
                }
            }

            // Расходы по категориям
            item {
                SectionHeader(text = stringResource(R.string.expenses_by_category))
                AppCard {
                    if (state.expenseCategories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.no_data),
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CategoryDonutChart(
                                segments = expenseSegments,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                            ) {
                                state.expenseCategories.forEachIndexed { index, item ->
                                    val segmentColor = expenseSegments.getOrNull(index)?.color
                                        ?: item.category.colorArgb?.let { Color(it.toInt()) }
                                        ?: IosRed
                                    CategoryLegendRow(
                                        color = segmentColor,
                                        name = item.category.name,
                                        amount = MoneyFormat.format(
                                            item.amountMinor,
                                            state.currencyCode
                                        ),
                                        percent = stringResource(
                                            R.string.percent_short,
                                            item.sharePercent.roundToInt(),
                                        ),
                                    )
                                    if (index < state.expenseCategories.lastIndex) {
                                        HorizontalDivider(
                                            color = colors.onSurface.copy(alpha = 0.05f),
                                            modifier = Modifier.padding(vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Тренд изменения баланса (Линейный график)
            item {
                SectionHeader(text = stringResource(R.string.balance_trend))
                AppCard {
                    BalanceTrendLineChart(dailyBars = state.dailyBars)
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.SpacingXl))
            }
        }
    }
}

// ── Интеллектуальный совет от Коппи (НОВЫЙ элемент) ──
@Composable
fun AnalyticsInsightCard(
    expenseTotal: Long,
    incomeTotal: Long,
    savingsPercent: Int,
) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme

    val insightText = remember(expenseTotal, incomeTotal, savingsPercent) {
        when {
            expenseTotal > incomeTotal && incomeTotal > 0 -> {
                "Упс, в этом периоде вы тратите больше, чем зарабатываете. Коппи советует проверить лимиты категорий!"
            }

            savingsPercent >= 20 -> {
                "Отличный результат! Вы сохранили $savingsPercent% бюджета. Коппи хвалит вас за разумную экономию."
            }

            savingsPercent in 1..19 -> {
                "Вы накопили $savingsPercent% от доходов. Небольшой шаг вперед — это тоже отличный шаг к цели!"
            }

            else -> {
                "Ведите учет регулярно. Коппи готов помочь вам отследить каждую деталь бюджета!"
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.onSurface.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Милый золотой маскот / Иконка Коппи
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(IosOrange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.TipsAndUpdates,
                contentDescription = null,
                tint = IosOrange,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Анализ от Коппи",
                style = typography.labelMedium,
                color = IosOrange,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = insightText,
                style = typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = colors.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

// ── Переключатель периодов Segmented Control ────────
@Composable
private fun PeriodTabRow(
    selectedPeriod: StatsPeriod,
    onSelect: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.onSurface.copy(alpha = 0.06f))
            .padding(2.dp),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            periodTabs.forEach { period ->
                val selected = period == selectedPeriod

                // Компактные сокращения для предотвращения наложения текста
                val shortLabel = when (period) {
                    StatsPeriod.DAY -> "День"
                    StatsPeriod.WEEK -> "Нед"
                    StatsPeriod.MONTH -> "Мес"
                    StatsPeriod.YEAR -> "Год"
                    StatsPeriod.CUSTOM -> "План"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) colors.surface else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enabled,
                        ) { if (enabled) onSelect(period) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = shortLabel,
                        style = typography.labelMedium.copy(fontSize = 12.sp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) colors.onSurface else colors.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// ── SummaryCard ────────────────────────────────────
@Composable
private fun SummaryCard(
    netTotal: Long,
    incomeTotal: Long,
    expenseTotal: Long,
    savingsPercent: Int,
    savingsRate: Float,
    currencyCode: String,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    AppCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Остаток бюджета",
                style = typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.45f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = when {
                    netTotal > 0L -> MoneyFormat.formatWithSignPrefix(
                        context,
                        netTotal,
                        currencyCode,
                        isPositive = true
                    )

                    netTotal < 0L -> MoneyFormat.formatWithSignPrefix(
                        context,
                        -netTotal,
                        currencyCode,
                        isPositive = false
                    )

                    else -> MoneyFormat.format(0L, currencyCode)
                },
                style = typography.headlineMedium.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Black,
                color = when {
                    netTotal > 0L -> IosGreen
                    netTotal < 0L -> IosRed
                    else -> colors.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatPill(
                    icon = Icons.Rounded.ArrowUpward,
                    iconTint = IosGreen,
                    label = stringResource(R.string.income),
                    value = MoneyFormat.formatWithSignPrefix(
                        context,
                        incomeTotal,
                        currencyCode,
                        isPositive = true
                    ),
                    valueColor = IosGreen,
                    modifier = Modifier.weight(1f),
                )
                StatPill(
                    icon = Icons.Rounded.ArrowDownward,
                    iconTint = IosRed,
                    label = stringResource(R.string.expenses),
                    value = MoneyFormat.format(expenseTotal, currencyCode),
                    valueColor = IosRed,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colors.onSurface.copy(alpha = 0.05f))
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SavingsRateRing(
                    savingsPercent = savingsPercent,
                    savingsRate = savingsRate,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LegendItem(color = IosGreen, label = stringResource(R.string.income))
                    Spacer(Modifier.size(16.dp))
                    LegendItem(color = IosRed, label = stringResource(R.string.expenses))
                }
            }
        }
    }
}

// ── StatPill ───────────────────────────────────────
@Composable
private fun StatPill(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(iconTint.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                style = typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp,
            )
        }
        Text(
            text = value,
            style = typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Контейнер для карточек (iOS-стиль) ──────────────
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.onSurface.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        content = content,
    )
}

// ── Заголовки секций (iOS-стиль) ───────────────────
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(horizontal = 24.dp),
    )
}

// ── Секция аналитики по счетам ─────────────────────
@Composable
fun AccountsAnalyticsSection(
    accounts: List<AccountAnalytics>,
    currencyCode: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (accounts.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.analytics_accounts_section),
                style = typography.titleMedium.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            if (accounts.size > 2) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = if (isExpanded)
                            stringResource(R.string.analytics_accounts_collapse)
                        else
                            stringResource(R.string.analytics_accounts_expand),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosBlue,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AppCard {
            val visibleAccounts = if (isExpanded || accounts.size <= 2) accounts
            else accounts.take(2)

            visibleAccounts.forEachIndexed { index, item ->
                AccountAnalyticsRow(item = item, currencyCode = currencyCode)
                if (index < visibleAccounts.lastIndex) {
                    HorizontalDivider(
                        color = colors.onSurface.copy(alpha = 0.05f),
                        modifier = Modifier.padding(start = 68.dp),
                    )
                }
            }

            if (accounts.size > 1) {
                HorizontalDivider(color = colors.onSurface.copy(alpha = 0.05f))
                AccountAnalyticsTotalRow(accounts = accounts, currencyCode = currencyCode)
            }
        }
    }
}

@Composable
fun AccountAnalyticsRow(
    item: AccountAnalytics,
    currencyCode: String,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val accountColor = colorFromHexOrDefault(item.account.colorHex, colors.primary)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accountColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.account.emoji, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.account.name,
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.analytics_accounts_tx_count,
                        item.transactionCount
                    ),
                    style = typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.4f),
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MoneyFormat.format(item.balanceMinor, item.account.currencyCode),
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        item.balanceMinor > 0 -> IosGreen
                        item.balanceMinor < 0 -> IosRed
                        else -> colors.onSurface
                    },
                )
                Text(
                    text = stringResource(item.account.type.localizedNameRes()),
                    style = typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AccountStatBar(
                dotColor = IosGreen,
                label = stringResource(R.string.analytics_income),
                value = MoneyFormat.format(item.incomeMinor, item.account.currencyCode),
                valueColor = IosGreen,
                progress = item.incomeShare,
                progressColor = IosGreen,
                modifier = Modifier.weight(1f),
            )
            AccountStatBar(
                dotColor = IosRed,
                label = stringResource(R.string.analytics_expense),
                value = MoneyFormat.format(item.expenseMinor, item.account.currencyCode),
                valueColor = IosRed,
                progress = item.expenseShare,
                progressColor = IosRed,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AccountStatBar(
    dotColor: Color,
    label: String,
    value: String,
    valueColor: Color,
    progress: Float,
    progressColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Text(
                text = label,
                style = typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp,
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.08f),
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
fun AccountAnalyticsTotalRow(
    accounts: List<AccountAnalytics>,
    currencyCode: String,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val totalIncome = accounts.sumOf { it.incomeMinor }
    val totalExpense = accounts.sumOf { it.expenseMinor }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.onSurface.copy(alpha = 0.015f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.analytics_accounts_total, accounts.size),
            style = typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = MoneyFormat.formatWithSignPrefix(
                    context,
                    totalIncome,
                    currencyCode,
                    isPositive = true
                ),
                style = typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = IosGreen,
            )
            Text(
                text = MoneyFormat.formatWithSignPrefix(
                    context,
                    totalExpense,
                    currencyCode,
                    isPositive = false
                ),
                style = typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = IosRed,
            )
        }
    }
}

// ── Индикатор сбережений (Arc Ring) ───────────────
@Composable
internal fun SavingsRateRing(
    savingsPercent: Int,
    savingsRate: Float,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val ringStartAngle = 140f
    val ringSweep = 260f

    val animatedRate = remember { Animatable(0f) }
    LaunchedEffect(savingsRate) {
        animatedRate.animateTo(
            targetValue = savingsRate.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = 12.dp.toPx()
            val diameter = size.minDimension - stroke * 1.1f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = colors.onSurface.copy(alpha = 0.05f),
                startAngle = ringStartAngle,
                sweepAngle = ringSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (animatedRate.value > 0f) {
                drawArc(
                    color = IosGreen,
                    startAngle = ringStartAngle,
                    sweepAngle = ringSweep * animatedRate.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$savingsPercent%",
                style = typography.headlineMedium.copy(fontSize = 26.sp),
                fontWeight = FontWeight.Black,
                color = colors.onSurface,
            )
            Text(
                text = stringResource(R.string.saved),
                style = typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
internal fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── Легенда категорий ─────────────────────────────
@Composable
internal fun CategoryLegendRow(
    color: Color,
    name: String,
    amount: String,
    percent: String,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = name,
            style = typography.bodySmall,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            maxLines = 1,
        )
        Text(
            text = amount,
            style = typography.bodySmall,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 10.dp),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = percent,
                style = typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
            )
        }
    }
}

// ── Круговой чарт категорий (Donut Chart) ──────────
@Composable
internal fun CategoryDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(segments) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        )
    }

    Box(
        modifier = modifier.size(150.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            val strokeWidth = 14.dp.toPx() // Более тонкая линия для iOS-стиля
            val gapAngle = 1.5f
            val diameter = size.minDimension - strokeWidth * 1.1f
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            if (segments.isEmpty()) {
                drawArc(
                    color = colors.onSurface.copy(alpha = 0.05f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth),
                )
                return@Canvas
            }

            val progress = animatedProgress.value
            var startAngle = -90f
            segments.forEach { segment ->
                val fullSweep = 360f * segment.fraction
                val sweep = (fullSweep - gapAngle).coerceAtLeast(0f) * progress
                if (sweep > 0f) {
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth),
                    )
                }
                startAngle += fullSweep
            }
        }
    }
}

// ── График расходов по дням ────────────────────────
@Composable
internal fun ExpenseColumnChart(
    dailyBars: List<AnalyticsDailyBar>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(dailyBars) { dailyBars.map { it.label } }

    if (dailyBars.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        return
    }

    LaunchedEffect(dailyBars) {
        val values = dailyBars.map { it.expenseMinor / 100.0 }
        if (values.isNotEmpty()) {
            modelProducer.runTransaction { columnSeries { series(values) } }
        }
    }

    val bottomFormatter = CartesianValueFormatter { _, value, _ ->
        labels.getOrNull(value.toInt()) ?: ""
    }
    val startFormatter = CartesianValueFormatter { _, value, _ ->
        if (value >= 1000) "${(value / 1000).toInt()}k₽" else "${value.toInt()}₽"
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(IosRed),
                        thickness = 8.dp, // Тонкие аккуратные столбцы
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    ),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = startFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(170.dp)
            .padding(16.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = dailyBars.size > 7),
    )
}

// ── График тренда баланса ──────────────────────────
@Composable
internal fun BalanceTrendLineChart(
    dailyBars: List<AnalyticsDailyBar>,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val modelProducer = remember { CartesianChartModelProducer() }
    val labels = remember(dailyBars) { dailyBars.map { it.label } }
    val trendValues = remember(dailyBars) { buildBalanceTrendValues(dailyBars) }

    if (dailyBars.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        return
    }

    LaunchedEffect(trendValues) {
        if (trendValues.isNotEmpty()) {
            modelProducer.runTransaction { lineSeries { series(trendValues) } }
        }
    }

    val bottomFormatter = CartesianValueFormatter { _, value, _ ->
        labels.getOrNull(value.toInt()) ?: ""
    }
    val startFormatter = CartesianValueFormatter { _, value, _ ->
        if (value >= 1000) "${(value / 1000).toInt()}k₽" else "${value.toInt()}₽"
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(IosBlue)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(
                                Brush.verticalGradient(
                                    listOf(
                                        IosBlue.copy(alpha = 0.22f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = startFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .height(150.dp)
            .padding(16.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = dailyBars.size > 7),
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────
fun colorFromHexOrDefault(hex: String?, default: Color): Color {
    if (hex.isNullOrBlank()) return default
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        default
    }
}

private fun startOfDayOffset(field: Int, amount: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.add(field, amount)
    return calendar.timeInMillis
}

private fun formatEpochMillis(millis: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale("ru")).format(Date(millis))
