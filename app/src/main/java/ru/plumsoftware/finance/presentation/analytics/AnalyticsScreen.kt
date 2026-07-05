package ru.plumsoftware.finance.presentation.analytics

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.common.StatsPeriod
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

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
    viewModel: AnalyticsViewModel = koinViewModel(),
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

    // ── Date range bottom sheet ────────────────────────────
    if (showDateRangePicker || state.showDateRangePicker) {
        ModalBottomSheet(
            onDismissRequest = {
                showDateRangePicker = false
                viewModel.dismissDateRangePicker()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface,
            shape = RoundedCornerShape(topStart = Dimens.RadiusXl, topEnd = Dimens.RadiusXl),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = Dimens.SpacingS)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                        .background(colors.surfaceVariant),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .heightIn(min = 580.dp)
                    .navigationBarsPadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Dimens.SpacingL,
                            end = Dimens.SpacingM,
                            top = Dimens.SpacingM,
                            bottom = Dimens.SpacingXs,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.pick_date_range),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = {
                        showDateRangePicker = false
                        viewModel.dismissDateRangePicker()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }

                DateRangePicker(
                    state = dateRangePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.surface,
                        titleContentColor = colors.onSurfaceVariant,
                        headlineContentColor = colors.onSurface,
                        weekdayContentColor = colors.onSurfaceVariant,
                        selectedDayContainerColor = colors.primary,
                        selectedDayContentColor = Color.White,
                        dayInSelectionRangeContainerColor = colors.primary.copy(alpha = 0.12f),
                        dayInSelectionRangeContentColor = colors.primary,
                        todayContentColor = colors.primary,
                        todayDateBorderColor = colors.primary,
                    ),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )

                val todayStart = startOfDayMillis(System.currentTimeMillis())
                val presets = listOf(
                    R.string.preset_week to (startOfDayOffset(Calendar.DAY_OF_YEAR, -7) to todayStart),
                    R.string.preset_month to (startOfDayOffset(Calendar.MONTH, -1) to todayStart),
                    R.string.preset_3m to (startOfDayOffset(Calendar.MONTH, -3) to todayStart),
                    R.string.preset_year to (startOfDayOffset(Calendar.YEAR, -1) to todayStart),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.SpacingL),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                    modifier = Modifier.padding(bottom = Dimens.SpacingS),
                ) {
                    items(presets, key = { it.first }) { (labelRes, range) ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                dateRangePickerState.setSelection(
                                    startOfDayMillis(range.first),
                                    startOfDayMillis(range.second),
                                )
                            },
                            label = { Text(stringResource(labelRes), style = typography.bodySmall) },
                            shape = RoundedCornerShape(Dimens.RadiusPill),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = colors.surfaceVariant,
                                selectedBorderColor = Color.Transparent,
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.surfaceVariant,
                                labelColor = colors.onSurfaceVariant,
                            ),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
                ) {
                    OutlinedButton(
                        onClick = {
                            showDateRangePicker = false
                            viewModel.dismissDateRangePicker()
                        },
                        modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        border = BorderStroke(1.dp, colors.surfaceVariant),
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
                        modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            disabledContainerColor = colors.primary.copy(alpha = 0.3f),
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
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
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Sticky header ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(top = Dimens.statusBarInset),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Dimens.SpacingL, end = Dimens.SpacingM, bottom = Dimens.SpacingXs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.analytics),
                        style = typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showDateRangePicker = true }) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = stringResource(R.string.cd_pick_date_range),
                            tint = if (dateRangeActive) colors.primary else colors.onSurfaceVariant,
                            modifier = Modifier.size(Dimens.IconSizeM),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingXs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PeriodTabRow(
                        selectedPeriod = state.period,
                        onSelect = { viewModel.selectPeriod(it) },
                        enabled = !dateRangeActive,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (dateRangeActive) 0.38f else 1f),
                    )
                    Spacer(Modifier.width(Dimens.SpacingS))
                    AnimatedVisibility(visible = !dateRangeActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = viewModel::navigatePeriodBack,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronLeft,
                                    contentDescription = stringResource(R.string.cd_prev_period),
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(Dimens.IconSizeM),
                                )
                            }
                            Text(
                                text = state.periodLabel.orEmpty(),
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            IconButton(
                                onClick = viewModel::navigatePeriodForward,
                                enabled = state.canNavigateForward,
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = stringResource(R.string.cd_next_period),
                                    tint = if (state.canNavigateForward) colors.onSurfaceVariant else colors.outlineVariant,
                                    modifier = Modifier.size(Dimens.IconSizeM),
                                )
                            }
                        }
                    }
                }

                // Custom range chip
                AnimatedVisibility(
                    visible = dateRangeActive,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    val startLabel = state.customStartMillis?.let { formatEpochMillis(it, "d MMM") }.orEmpty()
                    val endLabel = state.customEndMillis?.let { formatEpochMillis(it, "d MMM yyyy") }.orEmpty()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingL)
                            .padding(bottom = Dimens.SpacingXs)
                            .clip(RoundedCornerShape(Dimens.RadiusM))
                            .background(colors.primary.copy(alpha = 0.08f))
                            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingXs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(Dimens.IconSizeS),
                        )
                        Text(
                            text = stringResource(R.string.date_range_label, startLabel, endLabel),
                            style = typography.bodySmall,
                            color = colors.primary,
                            modifier = Modifier.weight(1f).padding(horizontal = Dimens.SpacingXs),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(
                            onClick = { viewModel.selectPeriod(StatsPeriod.MONTH) },
                            modifier = Modifier.size(Dimens.IconSizeL),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cd_clear_date_range),
                                tint = colors.primary,
                                modifier = Modifier.size(Dimens.IconSizeS),
                            )
                        }
                    }
                }

                HorizontalDivider(color = colors.surfaceVariant)
            }

            // ── Scrollable content ─────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = Dimens.SpacingM,
                    bottom = Dimens.SpacingXxl,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
            ) {

                // 1. Summary card
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

                // 2. Accounts section
                item {
                    AccountsAnalyticsSection(
                        accounts = state.accountAnalytics,
                        currencyCode = state.currencyCode,
                        isExpanded = state.isAccountsSectionExpanded,
                        onToggleExpand = viewModel::toggleAccountsSection,
                    )
                }

                // 3. Expenses by day chart
                item {
                    SectionLabel(
                        text = stringResource(R.string.expenses_by_day),
                        modifier = Modifier.padding(start = 0.dp),
                    )
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingL),
                    ) {
                        ExpenseColumnChart(dailyBars = state.dailyBars)
                    }
                }

                // 4. Expenses by category
                item {
                    SectionLabel(
                        text = stringResource(R.string.expenses_by_category),
                        modifier = Modifier.padding(start = 0.dp),
                    )
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingL),
                    ) {
                        if (state.expenseCategories.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
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
                                modifier = Modifier.padding(Dimens.SpacingM),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CategoryDonutChart(
                                    segments = expenseSegments,
                                    modifier = Modifier.padding(vertical = Dimens.SpacingS),
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = Dimens.SpacingS),
                                ) {
                                    state.expenseCategories.forEachIndexed { index, item ->
                                        val segmentColor = expenseSegments.getOrNull(index)?.color
                                            ?: item.category.colorArgb?.let { Color(it.toInt()) }
                                            ?: colors.error
                                        CategoryLegendRow(
                                            color = segmentColor,
                                            name = item.category.name,
                                            amount = MoneyFormat.format(item.amountMinor, state.currencyCode),
                                            percent = stringResource(
                                                R.string.percent_short,
                                                item.sharePercent.roundToInt(),
                                            ),
                                        )
                                        if (index < state.expenseCategories.lastIndex) {
                                            HorizontalDivider(
                                                color = colors.surfaceVariant,
                                                modifier = Modifier.padding(vertical = 2.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Balance trend
                item {
                    SectionLabel(
                        text = stringResource(R.string.balance_trend),
                        modifier = Modifier.padding(start = 0.dp),
                    )
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingL),
                    ) {
                        BalanceTrendLineChart(dailyBars = state.dailyBars)
                    }
                }
            }
        }
    }
}

// ── Period tab row ────────────────────────────────────────────────────────────
// Unchanged — already looks good

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
            .height(34.dp)
            .clip(RoundedCornerShape(Dimens.RadiusPill))
            .background(colors.surfaceVariant)
            .padding(2.dp),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            periodTabs.forEach { period ->
                val selected = period == selectedPeriod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                        .background(if (selected) colors.primary else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enabled,
                        ) { if (enabled) onSelect(period) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(period.labelRes()),
                        style = typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Color.White else colors.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@StringRes
private fun StatsPeriod.labelRes(): Int = when (this) {
    StatsPeriod.DAY -> R.string.period_day
    StatsPeriod.WEEK -> R.string.period_week
    StatsPeriod.MONTH -> R.string.period_month
    StatsPeriod.YEAR -> R.string.period_year
    StatsPeriod.CUSTOM -> R.string.period_custom
}

// ── SummaryCard ───────────────────────────────────────────────────────────────
// Redesigned: compact stat row on top, ring centred, cleaner spacing

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

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SpacingL),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingL),
        ) {
            // Net balance — smaller than before, with colour
            Text(
                text = when {
                    netTotal > 0L -> MoneyFormat.formatWithSignPrefix(context, netTotal, currencyCode, isPositive = true)
                    netTotal < 0L -> MoneyFormat.formatWithSignPrefix(context, -netTotal, currencyCode, isPositive = false)
                    else -> MoneyFormat.format(0L, currencyCode)
                },
                style = typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    netTotal > 0L -> colors.secondary
                    netTotal < 0L -> colors.error
                    else -> colors.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(Dimens.SpacingS))

            // Income / Expense row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
            ) {
                StatPill(
                    icon = Icons.Rounded.ArrowUpward,
                    iconTint = colors.secondary,
                    label = stringResource(R.string.income),
                    value = MoneyFormat.formatWithSignPrefix(context, incomeTotal, currencyCode, isPositive = true),
                    valueColor = colors.secondary,
                    modifier = Modifier.weight(1f),
                )
                StatPill(
                    icon = Icons.Rounded.ArrowDownward,
                    iconTint = colors.error,
                    label = stringResource(R.string.expenses),
                    value = MoneyFormat.format(expenseTotal, currencyCode),
                    valueColor = colors.error,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Dimens.SpacingM))
            HorizontalDivider(color = colors.surfaceVariant)
            Spacer(Modifier.height(Dimens.SpacingM))

            // Savings ring — centred
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SavingsRateRing(
                    savingsPercent = savingsPercent,
                    savingsRate = savingsRate,
                )
                Spacer(Modifier.height(Dimens.SpacingS))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LegendItem(color = colors.secondary, label = stringResource(R.string.income))
                    Spacer(Modifier.size(Dimens.SpacingM))
                    LegendItem(color = colors.error, label = stringResource(R.string.expenses))
                }
            }
        }
    }
}

// ── StatPill ──────────────────────────────────────────────────────────────────
// Replaces AnalyticsFinanceStat — pill-shaped background, cleaner look

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
            .clip(RoundedCornerShape(Dimens.RadiusM))
            .background(iconTint.copy(alpha = 0.07f))
            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS),
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
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
        Text(
            text = value,
            style = typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

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
