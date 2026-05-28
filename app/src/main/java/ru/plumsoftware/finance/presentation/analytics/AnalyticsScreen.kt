package ru.plumsoftware.finance.presentation.analytics

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.common.StatsPeriod
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.components.ios.IosChip
import ru.plumsoftware.finance.ui.components.ios.IosDateRangeSheet
import ru.plumsoftware.finance.ui.components.ios.IosSegmentedControl
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosOrange
import ru.plumsoftware.finance.ui.theme.IosPurple
import ru.plumsoftware.finance.ui.theme.IosRed
import ru.plumsoftware.finance.ui.theme.IosViolet

private val donutPalette = listOf(
    IosBlue,
    IosGreen,
    IosRed,
    IosPurple,
    IosOrange,
    IosViolet,
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
    val shapes = MaterialTheme.shapes

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshCurrentPeriod()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val periodLabels = listOf(
        StatsPeriod.DAY to stringResource(R.string.period_day),
        StatsPeriod.WEEK to stringResource(R.string.period_week),
        StatsPeriod.MONTH to stringResource(R.string.period_month),
        StatsPeriod.YEAR to stringResource(R.string.period_year),
    )
    var disabledExpense by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var disabledIncome by rememberSaveable { mutableStateOf(setOf<Long>()) }

    val expenseCategories = state.expenseCategories
    val incomeCategories = state.incomeCategories
    val activeExpenseCategories = expenseCategories.filterNot { it.category.id in disabledExpense }
    val activeIncomeCategories = incomeCategories.filterNot { it.category.id in disabledIncome }
    val expenseTotal = activeExpenseCategories.sumOf { it.amountMinor }
    val incomeTotal = activeIncomeCategories.sumOf { it.amountMinor }
    val savingsTotal = state.savingsIndex.totalSmartSavingsMinor
    val net = incomeTotal - expenseTotal
    val savingsPercent =
        if (incomeTotal <= 0L) 0 else (((incomeTotal - expenseTotal).coerceAtLeast(0L) * 100L) / incomeTotal).toInt()

    val showExpenseByDays = when (state.period) {
        StatsPeriod.DAY -> false
        StatsPeriod.CUSTOM -> {
            val start = state.customStartMillis
            val end = state.customEndMillis
            start != null && end != null && (end - start) > 86_400_000L
        }

        else -> true
    }

    if (state.showDateRangePicker) {
        IosDateRangeSheet(
            onDismiss = viewModel::dismissDateRangePicker,
            onConfirm = viewModel::applyCustomRange,
            initialStartMillis = state.customStartMillis,
            initialEndMillis = state.customEndMillis,
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.paddingMedium,
                end = Dimens.paddingMedium,
                top = Dimens.statusBarInset,
                bottom = Dimens.paddingMicro,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSection),
        ) {
            item {
                Text(
                    text = stringResource(R.string.analytics_title),
                    style = typography.headlineLarge,
                    color = colors.onSurface,
                )
            }
            item {
                IosSegmentedControl(
                    labels = periodLabels.map { it.second },
                    selectedIndex = periodLabels.indexOfFirst { it.first == state.period }.let { if (it >= 0) it else 2 },
                    onSelectIndex = { idx -> viewModel.selectPeriod(periodLabels[idx].first) },
                    modifier = Modifier.padding(top = Dimens.spacingList),
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.paddingLarge, vertical = Dimens.spacingRow),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMicro),
                    ) {
                        IosTextButton(
                            text = stringResource(R.string.analytics_nav_prev),
                            onClick = viewModel::navigatePeriodBack,
                            style = typography.titleMedium,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        )
                        AnimatedContent(
                            targetState = state.periodLabel.orEmpty(),
                            transitionSpec = {
                                (slideInHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth / 2 } + fadeIn(
                                    animationSpec = tween(250)
                                )).togetherWith(
                                    slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth / 2 } + fadeOut(
                                        animationSpec = tween(250)
                                    )
                                )
                            },
                            label = "period_label_transition",
                        ) { label ->
                            Text(
                                text = label,
                                color = colors.onSurfaceVariant,
                                style = typography.bodyMedium,
                                maxLines = 1,
                            )
                        }
                        IosTextButton(
                            text = stringResource(R.string.analytics_nav_next),
                            onClick = viewModel::navigatePeriodForward,
                            enabled = state.canNavigateForward,
                            style = typography.titleMedium,
                            color = if (state.canNavigateForward) colors.secondary else colors.outlineVariant,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(shapes.small)
                            .background(
                                color = if (state.period == StatsPeriod.CUSTOM) colors.secondary else colors.secondary.copy(alpha = 0.12f),
                            )
                            .border(
                                width = Dimens.borderThin,
                                color = if (state.period == StatsPeriod.CUSTOM) Color.Transparent else colors.secondary.copy(alpha = 0.25f),
                                shape = shapes.small,
                            )
                            .clickable { viewModel.selectPeriod(StatsPeriod.CUSTOM) }
                            .padding(horizontal = Dimens.spacingList, vertical = Dimens.paddingMicro + 1.dp),
                    ) {
                        Text(
                            text = state.periodChipLabel,
                            color = if (state.period == StatsPeriod.CUSTOM) colors.onSecondary else colors.secondary,
                            style = typography.bodySmall,
                        )
                    }
                }
            }
            item {
                IosCard {
                    SectionLabel(text = stringResource(R.string.analytics_total_balance))
                    Text(
                        text = MoneyFormat.format(net, state.currencyCode, showSign = true),
                        style = typography.displayLarge,
                        color = when {
                            net > 0L -> colors.tertiary
                            net < 0L -> colors.error
                            else -> colors.onSurface
                        },
                    )
                    Spacer(Modifier.height(Dimens.paddingSmall))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.paddingSmall),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMicro),
                        ) {
                            Text(
                                text = stringResource(R.string.analytics_income_up),
                                color = colors.tertiary,
                                style = typography.labelMedium,
                            )
                            Text(
                                text = stringResource(R.string.income),
                                color = colors.onSurfaceVariant,
                                style = typography.bodySmall,
                                maxLines = 1,
                            )
                            Text(
                                text = MoneyFormat.format(incomeTotal, state.currencyCode),
                                color = colors.tertiary,
                                style = typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMicro),
                        ) {
                            Text(
                                text = stringResource(R.string.analytics_expense_down),
                                color = colors.error,
                                style = typography.labelMedium,
                            )
                            Text(
                                text = stringResource(R.string.expense),
                                color = colors.onSurfaceVariant,
                                style = typography.bodySmall,
                                maxLines = 1,
                            )
                            Text(
                                text = MoneyFormat.format(expenseTotal, state.currencyCode),
                                color = colors.error,
                                style = typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(Modifier.height(Dimens.paddingMedium))
                    DonutChart(
                        values = listOf(
                            incomeTotal.toFloat(),
                            expenseTotal.toFloat(),
                            savingsTotal.toFloat()
                        ),
                        colors = listOf(IosGreen, IosRed, IosBlue),
                        centerLabel = stringResource(R.string.analytics_saved_percent, savingsPercent),
                    )
                    Spacer(Modifier.height(Dimens.spacingList))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LegendItem(color = colors.tertiary, label = stringResource(R.string.income))
                        Spacer(Modifier.size(Dimens.paddingMedium))
                        LegendItem(color = colors.error, label = stringResource(R.string.expense))
                        Spacer(Modifier.size(Dimens.paddingMedium))
                        LegendItem(color = colors.secondary, label = stringResource(R.string.analytics_transfers))
                    }
                }
            }

            if (showExpenseByDays) {
                item {
                    IosCard {
                        SectionLabel(text = stringResource(R.string.analytics_expenses_by_day))
                        ExpenseByDayChart(
                            data = state.dailyBars,
                            color = IosRed,
                        )
                    }
                }
            }

            item {
                CategoryDonutCard(
                    title = stringResource(R.string.analytics_income_by_category),
                    categories = incomeCategories,
                    disabled = disabledIncome,
                    onToggle = { id ->
                        disabledIncome =
                            if (id in disabledIncome) disabledIncome - id else disabledIncome + id
                    },
                    currencyCode = state.currencyCode,
                )
            }
            item {
                CategoryDonutCard(
                    title = stringResource(R.string.analytics_expense_by_category),
                    categories = expenseCategories,
                    disabled = disabledExpense,
                    onToggle = { id ->
                        disabledExpense =
                            if (id in disabledExpense) disabledExpense - id else disabledExpense + id
                    },
                    currencyCode = state.currencyCode,
                )
            }

        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = MaterialTheme.colorScheme
    Text(
        text = text,
        color = colors.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
private fun LegendItem(color: Color, label: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMicro),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.pageIndicatorDotActive)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun DonutChart(
    values: List<Float>,
    colors: List<Color>,
    centerLabel: String,
) {
    val schemeColors = MaterialTheme.colorScheme
    val total = values.sum().coerceAtLeast(1f)
    val reveal by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "analytics_donut_reveal",
    )
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(176.dp)) {
            var start = -90f
            values.forEachIndexed { index, value ->
                val sweep = (value / total) * 360f * reveal
                drawArc(
                    color = colors.getOrElse(index) { IosBlue },
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 18f, cap = StrokeCap.Round),
                )
                start += sweep
            }
        }
        Text(
            text = centerLabel,
            color = schemeColors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ExpenseByDayChart(
    data: List<AnalyticsDailyBar>,
    color: Color,
) {
    val colors = MaterialTheme.colorScheme
    if (data.isEmpty()) {
        Text(
            text = stringResource(R.string.no_data),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    val reveal by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "expense_by_day_reveal",
    )
    val max = data.maxOf { it.expenseMinor }.coerceAtLeast(1L)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val width = size.width / data.size
        val barWidth = (width * 0.55f).coerceAtLeast(6f)
        data.forEachIndexed { index, item ->
            val height = (item.expenseMinor.toFloat() / max) * size.height * 0.9f * reveal
            val left = index * width + (width - barWidth) / 2
            drawRoundRect(
                color = color,
                topLeft = Offset(left, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(Dimens.cornerRadiusSegmentInner.toPx(), Dimens.cornerRadiusSegmentInner.toPx()),
            )
        }
    }
}

@Composable
private fun CategoryDonutCard(
    title: String,
    categories: List<CategorySpending>,
    disabled: Set<Long>,
    onToggle: (Long) -> Unit,
    currencyCode: String,
) {
    val colors = MaterialTheme.colorScheme
    val active = categories.filterNot { it.category.id in disabled }
    val total = active.sumOf { it.amountMinor }
    IosCard {
        SectionLabel(text = title)
        Spacer(Modifier.height(Dimens.paddingSmall))
        if (categories.isEmpty()) {
            Text(
                stringResource(R.string.no_data),
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            DonutChart(
                values = active.map { it.amountMinor.toFloat() },
                colors = active.indices.map { donutPalette[it % donutPalette.size] },
                centerLabel = MoneyFormat.format(total, currencyCode),
            )
            Spacer(Modifier.height(Dimens.spacingRow))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
            ) {
                categories.forEach { item ->
                    IosChip(
                        text = "${item.category.icon} ${item.category.name}",
                        selected = item.category.id !in disabled,
                        onClick = { onToggle(item.category.id) },
                    )
                }
            }
        }
    }
}
