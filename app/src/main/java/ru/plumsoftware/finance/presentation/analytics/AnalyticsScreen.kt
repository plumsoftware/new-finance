package ru.plumsoftware.finance.presentation.analytics

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
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
import ru.plumsoftware.finance.ui.components.ios.IosSegmentedControl
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
        containerColor = Color(0xFFF2F2F7),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.analytics_title),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                IosSegmentedControl(
                    labels = periodLabels.map { it.second },
                    selectedIndex = periodLabels.indexOfFirst { it.first == state.period }.let { if (it >= 0) it else 2 },
                    onSelectIndex = { idx -> viewModel.selectPeriod(periodLabels[idx].first) },
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(
                            onClick = viewModel::navigatePeriodBack,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Text(text = "‹", fontSize = 20.sp, color = Color(0xFF007AFF))
                        }
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
                                color = Color(0xFF8E8E93),
                                fontSize = 15.sp,
                                maxLines = 1,
                            )
                        }
                        IconButton(
                            onClick = viewModel::navigatePeriodForward,
                            enabled = state.canNavigateForward,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Text(
                                text = "›",
                                fontSize = 20.sp,
                                color = if (state.canNavigateForward) Color(0xFF007AFF) else Color(0xFFC7C7CC),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                color = if (state.period == StatsPeriod.CUSTOM) IosBlue else IosBlue.copy(alpha = 0.12f),
                            )
                            .border(
                                width = 1.dp,
                                color = if (state.period == StatsPeriod.CUSTOM) Color.Transparent else IosBlue.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(15.dp),
                            )
                            .clickable { viewModel.selectPeriod(StatsPeriod.CUSTOM) }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = state.periodChipLabel,
                            color = if (state.period == StatsPeriod.CUSTOM) Color.White else IosBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
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
                        color = when {
                            net > 0L -> IosGreen
                            net < 0L -> IosRed
                            else -> Color.Black
                        },
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = "↑", color = IosGreen, fontSize = 14.sp)
                            Text(
                                text = "Доходы",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp,
                                maxLines = 1,
                            )
                            Text(
                                text = MoneyFormat.format(incomeTotal, state.currencyCode),
                                color = IosGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(text = "↓", color = IosRed, fontSize = 14.sp)
                            Text(
                                text = "Расходы",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp,
                                maxLines = 1,
                            )
                            Text(
                                text = MoneyFormat.format(expenseTotal, state.currencyCode),
                                color = IosRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LegendItem(color = IosGreen, label = "Доходы")
                        Spacer(Modifier.size(16.dp))
                        LegendItem(color = IosRed, label = "Расходы")
                        Spacer(Modifier.size(16.dp))
                        LegendItem(color = IosBlue, label = "Переводы")
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
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF8E8E93),
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
