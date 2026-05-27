package ru.plumsoftware.finance.presentation.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.common.StatsPeriod
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.components.PeriodSelector
import ru.plumsoftware.finance.ui.components.ios.IosDateRangeSheet
import ru.plumsoftware.finance.ui.components.ios.IosTopBar
import ru.plumsoftware.finance.ui.theme.MascotAssets
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEmptyPeriod = state.summary.incomeMinor == 0L &&
        state.summary.expenseMinor == 0L &&
        state.topCategories.isEmpty()
    val periodLabels = mapOf(
        StatsPeriod.DAY to stringResource(R.string.period_day),
        StatsPeriod.WEEK to stringResource(R.string.period_week),
        StatsPeriod.MONTH to stringResource(R.string.period_month),
        StatsPeriod.YEAR to stringResource(R.string.period_year),
        StatsPeriod.CUSTOM to stringResource(R.string.period_custom),
    )

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { IosTopBar(title = stringResource(R.string.analytics_title)) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimens.paddingLarge,
                vertical = Dimens.paddingSmall,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
        ) {
            item {
                PeriodSelector(
                    selected = state.period,
                    onSelected = viewModel::selectPeriod,
                    labels = periodLabels,
                    customPeriodLabel = state.periodLabel,
                )
            }
            if (isEmptyPeriod && !state.isLoading) {
                item {
                    MascotEmptyState(
                        mascotRes = MascotAssets.emptyAnalytics,
                        title = stringResource(R.string.empty_analytics_title),
                        subtitle = stringResource(R.string.empty_analytics_subtitle),
                    )
                }
            } else {
                item {
                    AnimatedVisibility(
                        visible = !state.isLoading,
                        enter = fadeIn(tween(300)),
                        exit = fadeOut(tween(200)),
                    ) {
                        IosCard {
                            RowSummary(stringResource(R.string.income), state.summary.incomeMinor, state.currencyCode, IosGreen)
                            RowSummary(stringResource(R.string.expense), state.summary.expenseMinor, state.currencyCode, IosRed)
                            RowSummary(
                                stringResource(R.string.net),
                                state.summary.netMinor,
                                state.currencyCode,
                                MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                item {
                    Text(stringResource(R.string.expense_chart), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Dimens.paddingSmall))
                    ExpenseBarChart(state.dailyBars, IosBlue)
                }
                item {
                    Text(stringResource(R.string.top_categories), fontWeight = FontWeight.SemiBold)
                }
                items(state.topCategories) { item ->
                    IosCard {
                        Text("${item.category.icon} ${item.category.name}")
                        LinearProgressIndicator(
                            progress = { item.sharePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.paddingSmall),
                        )
                        Text(
                            "${MoneyFormat.format(item.amountMinor, state.currencyCode)} · ${item.sharePercent.toInt()}%",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
                item {
                    IosCard {
                        Text(stringResource(R.string.savings_index_title), fontWeight = FontWeight.SemiBold)
                        Text(
                            MoneyFormat.format(state.savingsIndex.totalSmartSavingsMinor, state.currencyCode),
                            color = IosGreen,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            stringResource(
                                R.string.savings_index_monthly,
                                MoneyFormat.format(state.savingsIndex.estimatedMonthlyReductionMinor, state.currencyCode),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowSummary(label: String, amount: Long, currency: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingMicro),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text(
            MoneyFormat.format(amount, currency, showSign = true),
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ExpenseBarChart(
    data: List<Pair<String, Long>>,
    barColor: Color,
) {
    if (data.isEmpty()) {
        Text(stringResource(R.string.no_data), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        return
    }
    val max = data.maxOf { it.second }.coerceAtLeast(1L)
    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val barWidth = size.width / (data.size * 1.6f)
        data.forEachIndexed { index, (_, value) ->
            val barHeight = (value.toFloat() / max) * size.height * 0.9f
            val left = index * barWidth * 1.6f + barWidth * 0.3f
            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f),
            )
        }
    }
}
