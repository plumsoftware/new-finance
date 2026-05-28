package ru.plumsoftware.finance.presentation.analytics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import ru.plumsoftware.finance.ui.components.ios.IosChip
import ru.plumsoftware.finance.ui.components.ios.IosDateRangeSheet
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed

private val donutPalette = listOf(
    IosBlue,
    IosGreen,
    IosRed,
    Color(0xFF5856D6),
    Color(0xFFFF9500),
    Color(0xFFAF52DE),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

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
        StatsPeriod.CUSTOM to (state.periodLabel ?: stringResource(R.string.period_custom)),
    )
    var disabledExpense by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var disabledIncome by rememberSaveable { mutableStateOf(setOf<Long>()) }

    val expenseCategories = state.expenseCategories.filterNot { it.category.id in disabledExpense }
    val incomeCategories = state.incomeCategories.filterNot { it.category.id in disabledIncome }
    val expenseTotal = expenseCategories.sumOf { it.amountMinor }
    val incomeTotal = incomeCategories.sumOf { it.amountMinor }
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
        containerColor = Color(0xFFF2F2F7),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.analytics_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                IosCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFE5E5EA),
                                shape = RoundedCornerShape(10.dp),
                            )
                            .padding(4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        periodLabels.forEach { (period, label) ->
                            val selected = period == state.period
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (selected) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp),
                                    )
                                    .clickable { viewModel.selectPeriod(period) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) Color.Black else Color(0xFF6B6B72),
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
            item {
                IosCard {
                    SectionLabel(text = "ОБЩИЙ БАЛАНС")
                    Text(
                        text = MoneyFormat.format(net, state.currencyCode, showSign = true),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) IosGreen else IosRed,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "↑ Доходы ${MoneyFormat.format(incomeTotal, state.currencyCode)}",
                            color = IosGreen
                        )
                        Text(
                            "↓ Расходы ${MoneyFormat.format(expenseTotal, state.currencyCode)}",
                            color = IosRed
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    DonutChart(
                        values = listOf(
                            incomeTotal.toFloat(),
                            expenseTotal.toFloat(),
                            savingsTotal.toFloat()
                        ),
                        colors = listOf(IosGreen, IosRed, IosBlue),
                        centerLabel = "$savingsPercent% сохранено",
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LegendDot("Доходы", IosGreen)
                        LegendDot("Расходы", IosRed)
                        LegendDot("Переводы", IosBlue)
                    }
                }
            }

            if (showExpenseByDays) {
                item {
                    IosCard {
                        SectionLabel(text = "РАСХОДЫ ПО ДНЯМ")
                        ExpenseByDayChart(
                            data = state.dailyBars,
                            color = IosRed,
                        )
                    }
                }
            }

            item {
                CategoryDonutCard(
                    title = "ДОХОДЫ ПО КАТЕГОРИЯМ",
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
                    title = "РАСХОДЫ ПО КАТЕГОРИЯМ",
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
    Text(
        text = text,
        color = Color(0xFF8E8E93),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun LegendDot(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape))
        Spacer(Modifier.size(6.dp))
        Text(text = text, color = Color(0xFF8E8E93))
    }
}

@Composable
private fun DonutChart(
    values: List<Float>,
    colors: List<Color>,
    centerLabel: String,
) {
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
        Text(text = centerLabel, color = Color(0xFF8E8E93))
    }
}

@Composable
private fun ExpenseByDayChart(
    data: List<AnalyticsDailyBar>,
    color: Color,
) {
    if (data.isEmpty()) {
        Text(text = "Нет данных", color = Color(0xFF8E8E93))
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
                cornerRadius = CornerRadius(8f, 8f),
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
    val active = categories.filterNot { it.category.id in disabled }
    val total = active.sumOf { it.amountMinor }
    IosCard {
        SectionLabel(text = title)
        Spacer(Modifier.height(8.dp))
        if (categories.isEmpty()) {
            Text("Нет данных", color = Color(0xFF8E8E93))
        } else {
            DonutChart(
                values = active.map { it.amountMinor.toFloat() },
                colors = active.indices.map { donutPalette[it % donutPalette.size] },
                centerLabel = MoneyFormat.format(total, currencyCode),
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
