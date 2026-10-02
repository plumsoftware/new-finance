package ru.plumsoftware.finance.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.report.ReportKind
import ru.plumsoftware.finance.domain.analytics.DateRange
import ru.plumsoftware.finance.domain.analytics.PeriodMode
import ru.plumsoftware.finance.presentation.common.CategoryColors
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.export.ReportExportSheet
import ru.plumsoftware.finance.ui.ads.NativeAdContainer
import ru.plumsoftware.finance.ui.ads.NativeAdSession
import ru.plumsoftware.finance.ui.ds.BarChart
import ru.plumsoftware.finance.ui.ds.ChartBar
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FChip
import ru.plumsoftware.finance.ui.ds.FProgressBar
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.KopiImage
import ru.plumsoftware.finance.ui.ds.LegendDot
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.PeriodPickerSheet
import ru.plumsoftware.finance.ui.ds.RootBottomInset
import ru.plumsoftware.finance.ui.ds.SectionTitle
import ru.plumsoftware.finance.ui.ds.SegmentedInk
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.ds.masked
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val snackbar = LocalMascotSnackbar.current
    var showPeriod by rememberSaveable { mutableStateOf(false) }
    var showReport by rememberSaveable { mutableStateOf(false) }
    var reopenReportAfterPeriod by rememberSaveable { mutableStateOf(false) }

    val lastCategoryMsg = stringResource(R.string.analytics_last_category)
    LaunchedEffect(event) {
        if (event == AnalyticsEvent.LastCategory) {
            snackbar.show(lastCategoryMsg, Kopi.THINKING)
            viewModel.consumeEvent()
        }
    }

    val today = LocalDate.now()
    val visibleRange = DateRange(state.range.start, minOf(state.range.end, today).coerceAtLeastDate(state.range.start))
    val periodTitle = periodTitle(state.mode, state.range)

    if (showPeriod) {
        PeriodPickerSheet(
            initial = if (state.mode == PeriodMode.CUSTOM) state.range else null,
            minDate = state.minDate,
            onApply = {
                viewModel.setCustomRange(it)
                showPeriod = false
                if (reopenReportAfterPeriod) {
                    reopenReportAfterPeriod = false
                    showReport = true
                }
            },
            onDismiss = {
                showPeriod = false
                if (reopenReportAfterPeriod) {
                    reopenReportAfterPeriod = false
                    showReport = true
                }
            },
        )
    }
    if (showReport) {
        ReportExportSheet(
            kind = ReportKind.REPORT,
            range = state.range,
            isWholeMonth = state.mode == PeriodMode.MONTH,
            periodLabel = periodTitle,
            excludedIds = state.excludedIds,
            excludedNames = state.excludedNames,
            onChangePeriod = {
                showReport = false
                reopenReportAfterPeriod = true
                showPeriod = true
            },
            onDismiss = { showReport = false },
        )
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(c.bg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = RootBottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "header") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.nav_analytics), style = FinanceType.headline, color = c.textPrimary, modifier = Modifier.weight(1f))
                Row(
                    Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(c.surface)
                        .clickable(role = Role.Button) { showReport = true }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_report), null, tint = c.primaryTonalText, modifier = Modifier.size(18.dp))
                    HSpace(6.dp)
                    Text(stringResource(R.string.analytics_report), style = FinanceType.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = c.primaryTonalText)
                }
            }
        }
        item(key = "segment") {
            val modes = listOf(PeriodMode.WEEK, PeriodMode.MONTH, PeriodMode.YEAR, PeriodMode.CUSTOM)
            SegmentedInk(
                options = listOf(
                    stringResource(R.string.analytics_week),
                    stringResource(R.string.analytics_month),
                    stringResource(R.string.analytics_year),
                    stringResource(R.string.analytics_period),
                ),
                selectedIndex = modes.indexOf(state.mode),
                onSelect = { i -> if (modes[i] == PeriodMode.CUSTOM) showPeriod = true else viewModel.setMode(modes[i]) },
            )
        }
        item(key = "range") {
            Row(
                Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(c.surface)
                    .border(1.dp, c.outline, RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button) { showPeriod = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_calendar), null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
                HSpace(6.dp)
                Text(DateFmt.range(visibleRange.start, visibleRange.end), style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Medium), color = c.textPrimary)
                HSpace(4.dp)
                Icon(painterResource(R.drawable.ic_chevron_down), null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
            }
        }
        item(key = "chart") { ChartCard(state, periodTitle) }
        item(key = "chips") { CategoryChips(state, viewModel::toggleCategory, viewModel::restoreAll) }
        item(key = "forecast") { ForecastBlock(state) }
        if (AppConfig.nativeAnalytics.isNotBlank() && !NativeAdSession.isDismissed(AppConfig.nativeAnalytics)) {
            item(key = "ad") { FCard(padding = PaddingValues(0.dp)) { NativeAdContainer(adUnitId = AppConfig.nativeAnalytics) } }
        }
        item(key = "bycat") { ByCategory(state, viewModel::toggleCategory) }
    }
}

private fun LocalDate.coerceAtLeastDate(min: LocalDate) = if (isBefore(min)) min else this

@Composable
private fun periodTitle(mode: PeriodMode, range: DateRange): String = when (mode) {
    PeriodMode.MONTH -> DateFmt.monthYear(range.start)
    PeriodMode.YEAR -> range.start.year.toString()
    else -> DateFmt.range(range.start, range.end)
}

@Composable
private fun ChartCard(state: AnalyticsUiState, periodTitle: String) {
    val c = FinanceTheme.colors
    val cur = state.currencyCode
    val compareText = when {
        state.mode == PeriodMode.YEAR -> stringResource(R.string.analytics_year_avg, masked(Money.formatRounded(state.monthlyAverage, cur)))
        state.comparePercent == null -> null
        state.comparePercent < 0 -> stringResource(R.string.analytics_less, -state.comparePercent)
        state.comparePercent > 0 -> stringResource(R.string.analytics_more, state.comparePercent)
        else -> stringResource(R.string.analytics_same)
    }
    val summary = stringResource(R.string.analytics_chart_summary, periodTitle, masked(Money.formatRounded(state.total, cur)), compareText.orEmpty())
    FCard {
        Text(stringResource(R.string.analytics_expenses_for, periodTitle), style = FinanceType.bodySmall, color = c.textSecondary)
        Text(masked(Money.formatRounded(state.total, cur)), style = FinanceType.headlineLarge, color = c.textPrimary)
        if (compareText != null) {
            Text(
                compareText,
                style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = when {
                    state.mode == PeriodMode.YEAR -> c.textSecondary
                    (state.comparePercent ?: 0) > 0 -> c.dangerText
                    else -> c.successText
                },
            )
        }
        VSpace(14.dp)
        BarChart(
            bars = state.bars.map { b ->
                ChartBar(
                    value = b.value,
                    kind = b.kind,
                    axisLabel = b.axisLabel,
                    tooltip = "${if (b.start == b.end) DateFmt.dayMonthShort(b.start) else DateFmt.monthYear(b.start)} · ${masked(Money.formatRounded(b.value, cur))}",
                )
            },
            summary = summary,
        )
        VSpace(10.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(c.barDefault, stringResource(R.string.analytics_legend_usual))
            LegendDot(c.warning, stringResource(R.string.analytics_legend_high))
            LegendDot(c.primary, stringResource(R.string.analytics_legend_now))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChips(state: AnalyticsUiState, onToggle: (Long) -> Unit, onRestore: () -> Unit) {
    val c = FinanceTheme.colors
    if (state.categories.isEmpty()) return
    Column {
        SectionTitle(
            stringResource(R.string.analytics_categories_in_stats),
            action = if (state.excludedIds.isNotEmpty()) stringResource(R.string.analytics_restore_all) else null,
            onAction = if (state.excludedIds.isNotEmpty()) onRestore else null,
        )
        VSpace(8.dp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.categories.forEach { s ->
                FChip(
                    text = if (s.excluded) s.category.name else "✓ ${s.category.name}",
                    selected = !s.excluded,
                    onClick = { onToggle(s.category.id) },
                    selectedBg = c.primaryTonalBg,
                    selectedText = c.primaryTonalText,
                    unselectedText = c.excludedText,
                    border = c.outline,
                    height = 34.dp,
                )
            }
        }
        if (state.excludedNames.isNotEmpty()) {
            VSpace(8.dp)
            Text(
                stringResource(R.string.analytics_excluded_note, state.excludedNames.joinToString(", ")),
                style = FinanceType.caption,
                color = c.textSecondary,
            )
        }
    }
}

@Composable
private fun ForecastBlock(state: AnalyticsUiState) {
    val c = FinanceTheme.colors
    val cur = state.currencyCode
    val budget = state.forecastBudget
    val over = budget != null && state.forecast > budget
    InkCard(radius = 24.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (state.forecastIsFinal) stringResource(R.string.analytics_period_total) else forecastTitle(state),
                    style = FinanceType.bodySmall,
                    color = c.onInkSecondary,
                )
                Text(
                    masked(Money.formatRounded(state.forecast, cur)),
                    style = FinanceType.headline.copy(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
                if (budget != null) {
                    Text(
                        if (!over) stringResource(R.string.home_forecast_ok, masked(Money.formatRounded(budget - state.forecast, cur)))
                        else stringResource(R.string.home_forecast_over, masked(Money.formatRounded(state.forecast - budget, cur))),
                        style = FinanceType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (over) c.onInkDangerText else c.onInkSuccess,
                    )
                }
                VSpace(6.dp)
                Text(
                    buildString {
                        append(stringResource(R.string.analytics_income_line, masked(Money.signed(state.income, true, cur))))
                        state.savedPercent?.takeIf { it > 0 }?.let { append(" · "); append(stringResource(R.string.analytics_saved_pct, it)) }
                    },
                    style = FinanceType.caption,
                    color = c.onInkSecondary,
                )
            }
            KopiImage(if (over) Kopi.THINKING else Kopi.HAPPY, 74.dp)
        }
    }
}

@Composable
private fun forecastTitle(state: AnalyticsUiState): String = when (state.mode) {
    PeriodMode.MONTH -> stringResource(R.string.analytics_forecast_month, monthGenitive(state.range.start))
    PeriodMode.WEEK -> stringResource(R.string.analytics_forecast_week)
    PeriodMode.YEAR -> stringResource(R.string.analytics_forecast_year)
    PeriodMode.CUSTOM -> stringResource(R.string.analytics_forecast_on, DateFmt.dayMonth(state.range.end))
}

/** «сентября» — родительный падеж месяца из «d MMMM». */
private fun monthGenitive(date: LocalDate): String = DateFmt.dayMonth(date).substringAfter(' ')

@Composable
private fun ByCategory(state: AnalyticsUiState, onToggle: (Long) -> Unit) {
    val c = FinanceTheme.colors
    if (state.categories.isEmpty()) {
        FCard {
            ru.plumsoftware.finance.ui.ds.EmptyState(
                title = stringResource(R.string.analytics_empty),
                pose = Kopi.THINKING,
                mascotSize = 72.dp,
            )
        }
        return
    }
    FCard {
        SectionTitle(stringResource(R.string.report_section_categories))
        VSpace(4.dp)
        state.categories.forEach { s ->
            val color = CategoryColors.of(s.category)
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .alpha(if (s.excluded) 0.42f else 1f)
                    .clickable(role = Role.Checkbox) { onToggle(s.category.id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmojiBadge(s.category.icon, color, size = 38.dp)
                HSpace(12.dp)
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.category.name, style = FinanceType.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(masked(Money.formatRounded(s.amount, state.currencyCode)), style = FinanceType.body.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary)
                    }
                    VSpace(6.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FProgressBar(s.relative, color, Modifier.weight(1f), height = 5.dp)
                        HSpace(10.dp)
                        Text(
                            if (s.excluded) stringResource(R.string.analytics_excluded_short) else "${(s.share * 100).toInt()}%",
                            style = FinanceType.caption,
                            color = c.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
