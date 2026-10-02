package ru.plumsoftware.finance.presentation.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.budget.Upcoming
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.RecurringFrequency
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.navigation.popBackStackOrHome
import ru.plumsoftware.finance.presentation.common.CategoryColors
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.ui.ads.AdBannerBottomBar
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.FSwitch
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.IconButton44
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.SwipeHint
import ru.plumsoftware.finance.ui.ds.SwipeHintButton
import ru.plumsoftware.finance.ui.ds.rememberSwipeHint
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.ds.masked
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.time.LocalDate
import kotlin.math.roundToLong

/** Месячный эквивалент суммы по периодичности. */
private fun RecurringTransaction.monthlyMinor(): Long = when (frequency) {
    RecurringFrequency.DAILY -> (amountMinor * 30.4).roundToLong()
    RecurringFrequency.WEEKLY -> (amountMinor * 52 / 12.0).roundToLong()
    RecurringFrequency.MONTHLY -> amountMinor
    RecurringFrequency.YEARLY -> amountMinor / 12
}

/** Повторяющиеся операции: сводка «в месяц», активные и на паузе, пауза переключателем, удаление свайпом. */
@Composable
fun RecurringScreen(
    navController: NavController,
    viewModel: RecurringViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsRepository: SettingsRepository = koinInject()
    val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val cur = settings.defaultCurrencyCode
    val c = FinanceTheme.colors
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<RecurringTransaction?>(null) }
    val showSwipeHint = rememberSwipeHint(SwipeHint.RECURRING, hasItems = state.items.isNotEmpty())
    val (active, paused) = remember(state.items) { state.items.partition { it.isActive } }
    val today = LocalDate.now()
    val nextDates = remember(state.items, today) {
        Upcoming.occurrences(state.items, today, 400, DateFmt::toLocalDate, includeIncome = true)
            .groupBy { it.recurringId }.mapValues { it.value.first().date }
    }

    if (showAdd) {
        AddRecurringSheet(
            categories = state.categories,
            currencyCode = cur,
            onDismiss = { showAdd = false },
            onSave = {
                viewModel.add(it)
                showAdd = false
            },
        )
    }
    toDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = c.surface,
            title = { Text(stringResource(R.string.recurring_delete_title, item.title), style = FinanceType.titleLarge, color = c.textPrimary) },
            text = { Text(stringResource(R.string.recurring_delete_text), style = FinanceType.bodySmall, color = c.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(item.id)
                    toDelete = null
                }) { Text(stringResource(R.string.delete), color = c.dangerText) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text(stringResource(R.string.cancel), color = c.textSecondary) } },
        )
    }

    Scaffold(
        containerColor = c.bg,
        topBar = {
            SubScreenAppBar(
                title = stringResource(R.string.settings_recurring),
                onBack = { navController.popBackStackOrHome() },
                actions = {
                    SwipeHintButton(showSwipeHint)
                    IconButton44(R.drawable.ic_add, stringResource(R.string.add_recurring), { showAdd = true })
                },
            )
        },
        bottomBar = { AdBannerBottomBar(adUnitId = AppConfig.bannerRecurring) },
    ) { padding ->
        if (state.items.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.no_recurring),
                text = stringResource(R.string.no_recurring_desc),
                pose = Kopi.THINKING,
                action = stringResource(R.string.add_recurring),
                onAction = { showAdd = true },
                modifier = Modifier.padding(padding).padding(top = 48.dp),
            )
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            item {
                val expenses = active.filter { !it.isIncome }
                val nearest = expenses.mapNotNull { r -> nextDates[r.id]?.let { r to it } }.minByOrNull { it.second }
                InkCard(radius = 24.dp) {
                    Text(stringResource(R.string.recurring_per_month), style = FinanceType.bodySmall, color = c.onInkSecondary)
                    Text(masked(Money.formatRounded(expenses.sumOf { it.monthlyMinor() }, cur)), style = FinanceType.headlineLarge, color = Color.White)
                    if (nearest != null) {
                        Text(
                            stringResource(
                                R.string.recurring_nearest,
                                nearest.first.title,
                                DateFmt.dayMonth(nearest.second),
                                masked(Money.formatRounded(nearest.first.amountMinor, cur)),
                            ),
                            style = FinanceType.caption,
                            color = c.onInkSecondary,
                        )
                    }
                }
                VSpace(4.dp)
            }
            if (active.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.recurring_active)) }
                item { RecurringGroup(active, state.categoryMap, nextDates, cur, viewModel::toggleActive) { toDelete = it } }
            }
            if (paused.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.recurring_paused)) }
                item { RecurringGroup(paused, state.categoryMap, nextDates, cur, viewModel::toggleActive) { toDelete = it } }
            }
            item {
                VSpace(10.dp)
                Text(
                    stringResource(R.string.recurring_hint),
                    style = FinanceType.caption,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun RecurringGroup(
    items: List<RecurringTransaction>,
    categories: Map<Long, Category>,
    nextDates: Map<Long, LocalDate>,
    cur: String,
    onToggle: (Long, Boolean) -> Unit,
    onDeleteRequest: (RecurringTransaction) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FinanceTheme.colors.surface),
    ) {
        items.forEachIndexed { i, item ->
            if (i > 0) CardDivider(Modifier.padding(start = 70.dp))
            RecurringRow(item, categories[item.categoryId], nextDates[item.id], cur, { onToggle(item.id, it) }) { onDeleteRequest(item) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringRow(
    item: RecurringTransaction,
    category: Category?,
    next: LocalDate?,
    cur: String,
    onToggle: (Boolean) -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val c = FinanceTheme.colors
    val swipe = rememberSwipeToDismissBoxState()
    LaunchedEffect(swipe.currentValue) {
        if (swipe.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteRequest()
            swipe.reset()
        }
    }
    SwipeToDismissBox(
        state = swipe,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(c.danger).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Text(stringResource(R.string.delete), style = FinanceType.title, color = Color.White)
            }
        },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(c.surface)
                .heightIn(min = 68.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBadge(category?.icon ?: "🔁", CategoryColors.of(category), size = 42.dp, modifier = Modifier.alpha(if (item.isActive) 1f else 0.5f))
            HSpace(12.dp)
            Column(Modifier.weight(1f).alpha(if (item.isActive) 1f else 0.6f)) {
                Text(item.title, style = FinanceType.bodyMedium, color = c.textPrimary, maxLines = 2)
                Text(
                    buildString {
                        append(stringResource(item.frequency.labelRes))
                        if (item.isActive && next != null) {
                            append(" · ")
                            append(stringResource(R.string.next_date, DateFmt.dayMonthShort(next)))
                        }
                    },
                    style = FinanceType.caption,
                    color = c.textSecondary,
                )
            }
            HSpace(8.dp)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    masked(Money.signed(item.amountMinor, item.isIncome, cur)),
                    style = FinanceType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = if (item.isIncome) c.successText else c.textPrimary,
                )
                VSpace(4.dp)
                FSwitch(item.isActive, onToggle)
            }
        }
    }
}
