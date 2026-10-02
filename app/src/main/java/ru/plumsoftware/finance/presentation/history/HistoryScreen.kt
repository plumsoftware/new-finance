package ru.plumsoftware.finance.presentation.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.common.TxItem
import ru.plumsoftware.finance.presentation.dashboard.TxRow
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.FChip
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.SwipeHint
import ru.plumsoftware.finance.ui.ds.SwipeHintButton
import ru.plumsoftware.finance.ui.ds.rememberSwipeHint
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.ds.masked
import ru.plumsoftware.finance.ui.ds.LocalAmountVisibility
import ru.plumsoftware.finance.ui.ds.MASK
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.LocalDate

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onEdit: (Long) -> Unit = {},
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showSwipeHint = rememberSwipeHint(SwipeHint.HISTORY, hasItems = state.hasAny)

    // Индекс заголовка дня в списке: 3 фиксированных элемента (неделя, поиск, фильтры), затем группы.
    fun indexOfDay(date: LocalDate): Int? {
        var idx = 3
        state.groups.forEach { g ->
            if (g.date == date) return idx
            idx += 1
        }
        return null
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(
            title = stringResource(R.string.nav_history),
            onBack = onBack,
            actions = { if (state.hasAny) SwipeHintButton(showSwipeHint) },
        )
        if (!state.isLoading && !state.hasAny) {
            EmptyState(
                title = stringResource(R.string.history_empty_title),
                pose = Kopi.SLEEPING,
                action = stringResource(R.string.history_empty_action),
                onAction = onNavigateToAdd,
                modifier = Modifier.padding(top = 48.dp),
            )
            return@Column
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "week") {
                WeekBlock(state) { date ->
                    indexOfDay(date)?.let { scope.launch { listState.animateScrollToItem(it) } }
                }
            }
            item(key = "search") { SearchField(state.query, viewModel::setQuery) }
            item(key = "filters") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        HistoryFilter.ALL to R.string.history_filter_all,
                        HistoryFilter.EXPENSES to R.string.history_filter_expenses,
                        HistoryFilter.INCOME to R.string.history_filter_income,
                    ).forEach { (f, res) ->
                        FChip(stringResource(res), selected = state.filter == f, onClick = { viewModel.setFilter(f) })
                    }
                }
            }
            if (state.groups.isEmpty()) {
                item(key = "nothing") {
                    EmptyState(title = stringResource(R.string.history_nothing_found), pose = Kopi.THINKING)
                }
            }
            items(state.groups, key = { it.date.toEpochDay() }) { group ->
                DayGroupCard(group, state.currencyCode, onEdit = onEdit, onDelete = viewModel::deleteTransaction)
            }
        }
    }
}

@Composable
private fun WeekBlock(state: HistoryUiState, onDay: (LocalDate) -> Unit) {
    val c = FinanceTheme.colors
    InkCard(radius = 24.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.history_this_week, DateFmt.range(state.weekStart, state.weekEnd)),
                style = FinanceType.bodySmall,
                color = c.onInkSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(masked(Money.formatRounded(state.weekTotal, state.currencyCode)), style = FinanceType.titleBold, color = Color.White)
        }
        VSpace(14.dp)
        val max = state.weekBars.maxOfOrNull { it.totalMinor }?.takeIf { it > 0 } ?: 1L
        val amountsHidden = LocalAmountVisibility.current.hidden
        val weekSummary = stringResource(
            R.string.a11y_week_chart,
            DateFmt.range(state.weekStart, state.weekEnd),
            masked(Money.formatRounded(state.weekTotal, state.currencyCode)),
            state.weekBars.filter { !it.isFuture }.joinToString(", ") {
                "${DateFmt.weekdayShort(it.date)} ${if (amountsHidden) MASK else Money.formatRounded(it.totalMinor, state.currencyCode)}"
            },
        )
        Row(
            Modifier
                .fillMaxWidth()
                .semantics { contentDescription = weekSummary },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            state.weekBars.forEach { bar ->
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button) { onDay(bar.date) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        if (bar.totalMinor > 0) masked(Money.short(bar.totalMinor)) else "",
                        style = FinanceType.axis,
                        color = c.onInkSecondary,
                        maxLines = 1,
                    )
                    VSpace(4.dp)
                    Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.BottomCenter) {
                        val h by animateFloatAsState(
                            if (bar.totalMinor <= 0) 0.05f else (bar.totalMinor.toFloat() / max).coerceIn(0.05f, 1f),
                            tween(350),
                            label = "wk",
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(h)
                                .background(
                                    when {
                                        bar.isToday -> Color.White
                                        bar.isFuture -> Color.White.copy(alpha = 0.14f)
                                        bar.aboveNorm -> c.onInkWarning
                                        else -> c.onInkBar
                                    },
                                    RoundedCornerShape(4.dp),
                                ),
                        )
                    }
                    VSpace(6.dp)
                    Text(
                        DateFmt.weekdayShort(bar.date),
                        style = FinanceType.micro,
                        color = if (bar.isToday) Color.White else c.onInkSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val c = FinanceTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(c.searchField, RoundedCornerShape(28.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_search), contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
        HSpace(10.dp)
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text(stringResource(R.string.history_search_hint), style = FinanceType.body, color = c.textSecondary)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = FinanceType.body.copy(color = c.textPrimary),
                cursorBrush = SolidColor(c.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.history_search_clear),
                tint = c.textSecondary,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onChange("") }
                    .padding(6.dp),
            )
        }
    }
}

@Composable
private fun dayTitle(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> stringResource(R.string.home_today)
        today.minusDays(1) -> stringResource(R.string.history_yesterday)
        else -> if (date.year == today.year) DateFmt.dayMonth(date) else "${DateFmt.dayMonth(date)} ${date.year}"
    }
}

@Composable
private fun DayGroupCard(group: DayGroup, currency: String, onEdit: (Long) -> Unit, onDelete: (Long) -> Unit) {
    val c = FinanceTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp, start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(dayTitle(group.date), style = FinanceType.titleSection, color = c.textPrimary, modifier = Modifier.weight(1f))
            if (group.expensesMinor > 0) {
                Text(masked(Money.signed(group.expensesMinor, false, currency)), style = FinanceType.bodyMedium, color = c.textSecondary)
            }
        }
        Column(
            Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(c.surface),
        ) {
            group.items.forEachIndexed { i, item ->
                if (i > 0) Box(Modifier.padding(start = 70.dp).fillMaxWidth().height(1.dp).background(c.divider))
                SwipeRow(item, onEdit, onDelete)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeRow(item: TxItem, onEdit: (Long) -> Unit, onDelete: (Long) -> Unit) {
    val c = FinanceTheme.colors
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) onDelete(item.id)
    }
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(c.danger)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(stringResource(R.string.delete), style = FinanceType.title, color = Color.White)
            }
        },
    ) {
        Box(Modifier.background(c.surface).padding(horizontal = 16.dp)) {
            TxRow(item, onClick = { onEdit(item.id) }, showAccount = true)
        }
    }
}
