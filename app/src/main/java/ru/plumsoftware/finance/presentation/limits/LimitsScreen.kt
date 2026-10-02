package ru.plumsoftware.finance.presentation.limits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.budget.BudgetMath
import ru.plumsoftware.finance.navigation.popBackStackOrHome
import ru.plumsoftware.finance.presentation.common.CategoryColors
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.dashboard.AmountEntrySheet
import ru.plumsoftware.finance.ui.ads.AdBannerBottomBar
import ru.plumsoftware.finance.ui.ds.ButtonTonal
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FProgressBar
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.ListRow
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.TextAction
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.LocalDate

private sealed interface LimitsSheet {
    data class Category(val item: LimitItem, val initial: Long) : LimitsSheet
    data object Budget : LimitsSheet
}

@Composable
fun LimitsScreen(navController: NavController, viewModel: LimitsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val cur = state.currencyCode
    val today = LocalDate.now()
    var sheet by remember { mutableStateOf<LimitsSheet?>(null) }

    when (val s = sheet) {
        is LimitsSheet.Category -> AmountEntrySheet(
            title = "${s.item.category.icon} ${s.item.category.name}",
            message = stringResource(R.string.limits_sheet_message, Money.formatRounded(s.item.spentMinor, cur)),
            confirmLabel = stringResource(R.string.save),
            dismissLabel = stringResource(R.string.cancel),
            initialMinor = s.initial,
            currencyCode = cur,
            onConfirm = {
                viewModel.setLimit(s.item.category.id, it)
                sheet = null
            },
            onDismiss = { sheet = null },
            secondaryAction = if (s.item.limitMinor != null) {
                stringResource(R.string.limits_remove) to {
                    viewModel.setLimit(s.item.category.id, null)
                    sheet = null
                }
            } else null,
        )
        LimitsSheet.Budget -> AmountEntrySheet(
            title = stringResource(R.string.limits_budget_title),
            message = stringResource(R.string.limits_budget_message),
            confirmLabel = stringResource(R.string.save),
            dismissLabel = stringResource(R.string.cancel),
            initialMinor = state.explicitBudgetMinor ?: state.budgetMinor,
            currencyCode = cur,
            onConfirm = {
                viewModel.setBudget(it)
                sheet = null
            },
            onDismiss = { sheet = null },
            secondaryAction = if (state.explicitBudgetMinor != null) {
                stringResource(R.string.limits_budget_reset) to {
                    viewModel.setBudget(null)
                    sheet = null
                }
            } else null,
        )
        null -> Unit
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            SubScreenAppBar(
                title = stringResource(R.string.limits),
                onBack = { navController.popBackStackOrHome() },
                actions = { Text(DateFmt.monthStandalone(today), style = FinanceType.bodySmall, color = c.textSecondary) },
            )
        },
        // Баннер в bottomBar: список получает нижний отступ = высоте баннера (§2 п.11).
        bottomBar = { AdBannerBottomBar(adUnitId = AppConfig.bannerLimits) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FCard(onClick = { sheet = LimitsSheet.Budget }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.limits_budget_title), style = FinanceType.caption, color = c.textSecondary)
                            Text(
                                if (state.budgetMinor > 0) Money.formatRounded(state.budgetMinor, cur) else stringResource(R.string.limits_budget_not_set),
                                style = FinanceType.titleBold,
                                color = c.textPrimary,
                            )
                            Text(
                                stringResource(
                                    when (state.budgetSource) {
                                        BudgetSource.EXPLICIT -> R.string.limits_budget_src_explicit
                                        BudgetSource.LIMITS -> R.string.limits_budget_src_limits
                                        BudgetSource.AVERAGE -> R.string.limits_budget_src_avg
                                        BudgetSource.NONE -> R.string.limits_budget_src_none
                                    },
                                ),
                                style = FinanceType.caption,
                                color = c.textSecondary,
                            )
                        }
                        TextAction(stringResource(R.string.goal_edit), { sheet = LimitsSheet.Budget })
                    }
                }
            }
            if (state.withLimit.isNotEmpty()) {
                item {
                    FCard {
                        Row(Modifier.fillMaxWidth()) {
                            SummaryCell(state.okCount, stringResource(R.string.limit_status_ok), c.successText, Modifier.weight(1f))
                            SummaryCell(state.almostCount, stringResource(R.string.limit_status_warning), c.warningText, Modifier.weight(1f))
                            SummaryCell(state.exceededCount, stringResource(R.string.limit_status_exceeded), c.dangerText, Modifier.weight(1f))
                        }
                    }
                }
                item { SectionHeader(stringResource(R.string.limits_with_limit)) }
                item {
                    FCard(padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                        state.withLimit.forEachIndexed { i, item ->
                            if (i > 0) CardDivider()
                            LimitRow(item, cur) { sheet = LimitsSheet.Category(item, item.limitMinor ?: item.suggestedMinor) }
                        }
                    }
                }
            }
            if (state.withoutLimit.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.limits_without_limit)) }
                item {
                    FCard(padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                        state.withoutLimit.forEachIndexed { i, item ->
                            if (i > 0) CardDivider()
                            ListRow(
                                title = item.category.name,
                                subtitle = stringResource(R.string.limits_spent_in_month, Money.formatRounded(item.spentMinor, cur), monthPrepositional(today)),
                                leading = { EmojiBadge(item.category.icon, CategoryColors.of(item.category), size = 42.dp) },
                                trailing = {
                                    ButtonTonal(
                                        stringResource(R.string.limits_set_action),
                                        onClick = { sheet = LimitsSheet.Category(item, item.suggestedMinor) },
                                        compact = true,
                                    )
                                },
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.limits_hint),
                    style = FinanceType.caption,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }
    }
}

/** «в сентябре» */
@Composable
private fun monthPrepositional(date: LocalDate): String =
    stringArrayResourceSafe(R.array.months_prepositional, date.monthValue - 1)

@Composable
private fun stringArrayResourceSafe(res: Int, index: Int): String =
    androidx.compose.ui.res.stringArrayResource(res).getOrElse(index) { "" }

@Composable
private fun SummaryCell(value: Int, label: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = FinanceType.headline.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold), color = color)
        Text(label, style = FinanceType.caption, color = FinanceTheme.colors.textSecondary)
    }
}

@Composable
private fun LimitRow(item: LimitItem, cur: String, onClick: () -> Unit) {
    val c = FinanceTheme.colors
    val limit = item.limitMinor ?: 0L
    val color = when (item.status) {
        BudgetMath.LimitStatus.OK -> c.success
        BudgetMath.LimitStatus.ALMOST -> c.warning
        BudgetMath.LimitStatus.EXCEEDED -> c.danger
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        ListRow(
            title = item.category.name,
            subtitle = "${Money.formatRounded(item.spentMinor, cur)} / ${Money.formatRounded(limit, cur)}",
            leading = { EmojiBadge(item.category.icon, CategoryColors.of(item.category), size = 42.dp) },
            value = if (item.spentMinor > limit) stringResource(R.string.limits_over_by, Money.formatRounded(item.spentMinor - limit, cur))
            else stringResource(R.string.limits_left, Money.formatRounded(limit - item.spentMinor, cur)),
            valueColor = if (item.spentMinor > limit) c.dangerText else c.textSecondary,
            onClick = onClick,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            HSpace(54.dp)
            FProgressBar(item.ratio, color, Modifier.weight(1f), height = 6.dp)
        }
        VSpace(10.dp)
    }
}
