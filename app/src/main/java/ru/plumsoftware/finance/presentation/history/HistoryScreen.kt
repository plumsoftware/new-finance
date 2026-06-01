package ru.plumsoftware.finance.presentation.history

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.transactions.TransactionRow
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.PrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.MascotAssets
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class HistoryFilter {
    ALL,
    INCOME,
    EXPENSE,
}

private data class HistoryGroup(
    val dateMillis: Long,
    val transactions: List<Transaction>,
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HistoryScreen(
    onBack: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var typeFilter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }
    var selectedCategoryIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val dateRangeActive = state.dateRangeActive

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = state.dateRangeStart
            ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        initialSelectedEndDateMillis = state.dateRangeEnd
            ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        initialDisplayMode = DisplayMode.Picker,
    )

    if (showDateRangePicker) {
        ModalBottomSheet(
            onDismissRequest = { showDateRangePicker = false },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            containerColor = colors.surface,
            shape = RoundedCornerShape(
                topStart = Dimens.RadiusXl,
                topEnd = Dimens.RadiusXl,
            ),
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
                    IconButton(onClick = { showDateRangePicker = false }) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                val now = LocalDate.now()
                val presets = listOf(
                    R.string.preset_week to (now.minusWeeks(1) to now),
                    R.string.preset_month to (now.minusMonths(1) to now),
                    R.string.preset_3m to (now.minusMonths(3) to now),
                    R.string.preset_year to (now.minusYears(1) to now),
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
                                val startMs = range.first
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                val endMs = range.second
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                dateRangePickerState.setSelection(startMs, endMs)
                            },
                            label = {
                                Text(
                                    text = stringResource(labelRes),
                                    style = typography.bodySmall,
                                )
                            },
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
                        .padding(
                            horizontal = Dimens.SpacingL,
                            vertical = Dimens.SpacingM,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
                ) {
                    OutlinedButton(
                        onClick = { showDateRangePicker = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        border = BorderStroke(
                            1.dp,
                            colors.surfaceVariant,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = colors.onSurface,
                        )
                    }

                    Button(
                        onClick = {
                            val startMs = dateRangePickerState.selectedStartDateMillis
                            val endMs = dateRangePickerState.selectedEndDateMillis
                            if (startMs != null && endMs != null) {
                                val toDate = { ms: Long ->
                                    Instant.ofEpochMilli(ms)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                viewModel.setDateRange(toDate(startMs), toDate(endMs))
                            }
                            showDateRangePicker = false
                        },
                        enabled = dateRangePickerState.selectedStartDateMillis != null &&
                            dateRangePickerState.selectedEndDateMillis != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.ButtonHeight),
                        shape = RoundedCornerShape(Dimens.RadiusL),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            disabledContainerColor = colors.primary.copy(alpha = 0.3f),
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.action_apply),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }

    val filterCategories = remember(state.transactions, state.categoryMap) {
        state.transactions
            .mapNotNull { tx -> tx.categoryId?.let { state.categoryMap[it] } }
            .distinctBy { it.id }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    val filteredTransactions = remember(
        state.transactions,
        typeFilter,
        selectedCategoryIds,
        state.categoryMap,
        state.dateRangeStart,
        state.dateRangeEnd,
    ) {
        state.transactions.filter { transaction ->
            matchesTypeFilter(transaction, typeFilter) &&
                matchesCategoryFilter(transaction, selectedCategoryIds) &&
                matchesDateRange(transaction, state.dateRangeStart, state.dateRangeEnd)
        }
    }

    val groups = remember(filteredTransactions) {
        val keyFormatter = SimpleDateFormat("yyyyMMdd", Locale.US)
        filteredTransactions
            .groupBy { keyFormatter.format(Date(it.dateMillis)) }
            .toList()
            .sortedByDescending { it.first }
            .map { (_, items) ->
                HistoryGroup(
                    dateMillis = items.first().dateMillis,
                    transactions = items,
                )
            }
    }

    selectedTransaction?.let { tx ->
        TransactionDetailSheet(
            transaction = tx,
            category = state.categoryMap[tx.categoryId],
            currencyCode = state.currencyCode,
            onDismiss = { selectedTransaction = null },
            onDelete = {
                viewModel.deleteTransaction(tx.id)
                selectedTransaction = null
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.history),
                backLabel = stringResource(R.string.nav_home),
                onBack = onBack,
            )
        },
    ) {
        if (state.transactions.isEmpty()) {
            HistoryEmptyState(
                modifier = Modifier.padding(it),
                onNavigateToAdd = onNavigateToAdd,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                contentPadding = PaddingValues(
                    top = Dimens.SpacingXs,
                    bottom = Dimens.SpacingM,
                ),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingL),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.last_n_transactions),
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                        IconButton(onClick = { showDateRangePicker = true }) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = stringResource(R.string.cd_pick_date_range),
                                tint = if (dateRangeActive) {
                                    colors.primary
                                } else {
                                    colors.onSurfaceVariant
                                },
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Dimens.SpacingL))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Dimens.SpacingL),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
                    ) {
                        item {
                            HistoryFilterChip(
                                label = stringResource(R.string.all),
                                selected = typeFilter == HistoryFilter.ALL && selectedCategoryIds.isEmpty(),
                                selectedColor = colors.primary,
                                onClick = {
                                    typeFilter = HistoryFilter.ALL
                                    selectedCategoryIds = emptySet()
                                },
                            )
                        }
                        item {
                            HistoryFilterChip(
                                label = stringResource(R.string.income),
                                selected = typeFilter == HistoryFilter.INCOME && selectedCategoryIds.isEmpty(),
                                selectedColor = colors.secondary,
                                onClick = {
                                    typeFilter = HistoryFilter.INCOME
                                    selectedCategoryIds = emptySet()
                                },
                            )
                        }
                        item {
                            HistoryFilterChip(
                                label = stringResource(R.string.expenses_label),
                                selected = typeFilter == HistoryFilter.EXPENSE && selectedCategoryIds.isEmpty(),
                                selectedColor = colors.error,
                                onClick = {
                                    typeFilter = HistoryFilter.EXPENSE
                                    selectedCategoryIds = emptySet()
                                },
                            )
                        }
                        items(filterCategories, key = { it.id }) { category ->
                            HistoryFilterChip(
                                label = category.name,
                                leadingEmoji = category.icon,
                                selected = selectedCategoryIds.contains(category.id),
                                selectedColor = categoryColor(category, colors),
                                onClick = {
                                    selectedCategoryIds = if (category.id in selectedCategoryIds) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                },
                            )
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = dateRangeActive,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        val startLabel = state.dateRangeStart?.format(
                            DateTimeFormatter.ofPattern("d MMM", Locale("ru")),
                        ).orEmpty()
                        val endLabel = state.dateRangeEnd?.format(
                            DateTimeFormatter.ofPattern("d MMM yyyy", Locale("ru")),
                        ).orEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = Dimens.SpacingL,
                                    vertical = Dimens.SpacingXs,
                                )
                                .clip(RoundedCornerShape(Dimens.RadiusM))
                                .background(colors.primary.copy(alpha = 0.08f))
                                .padding(
                                    horizontal = Dimens.SpacingM,
                                    vertical = Dimens.SpacingS,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DateRange,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(Dimens.IconSizeS),
                            )
                            Text(
                                text = stringResource(
                                    R.string.date_range_label,
                                    startLabel,
                                    endLabel,
                                ),
                                style = typography.bodyMedium,
                                color = colors.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = Dimens.SpacingS),
                            )
                            IconButton(
                                onClick = { viewModel.clearDateRange() },
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
                }

                if (groups.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingXxl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                        ) {
                            Text(
                                text = stringResource(R.string.no_operations_for_period),
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    groups.forEach { group ->
                        item(key = "header-${group.dateMillis}") {
                            Text(
                                text = formatDateHeader(group.dateMillis),
                                style = typography.labelMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = Dimens.SpacingL,
                                    end = Dimens.SpacingL,
                                    top = Dimens.SpacingM,
                                    bottom = Dimens.SpacingXs,
                                ),
                            )
                        }
                        item(key = "card-${group.dateMillis}") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = Dimens.SpacingL,
                                        vertical = Dimens.SpacingXxs,
                                    ),
                                shape = RoundedCornerShape(Dimens.RadiusL),
                                colors = CardDefaults.cardColors(
                                    containerColor = colors.surface,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                group.transactions.forEachIndexed { index, transaction ->
                                    SwipeableTransactionRow(
                                        transaction = transaction,
                                        category = state.categoryMap[transaction.categoryId],
                                        currencyCode = state.currencyCode,
                                        onClick = { selectedTransaction = transaction },
                                        onDelete = { viewModel.deleteTransaction(transaction.id) },
                                    )
                                    if (index < group.transactions.lastIndex) {
                                        HorizontalDivider(
                                            color = colors.surfaceVariant,
                                            thickness = Dimens.borderThin,
                                            modifier = Modifier.padding(start = 72.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEmptyState(
    modifier: Modifier = Modifier,
    onNavigateToAdd: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = Dimens.SpacingM,
                start = Dimens.SpacingL,
                end = Dimens.SpacingL,
                bottom = Dimens.SpacingL,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(MascotAssets.emptyTransactions),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(Dimens.SpacingL))
        Text(
            text = stringResource(R.string.no_operations_yet),
            style = typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.no_operations_for_period),
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.SpacingL))
        PrimaryButton(
            text = stringResource(R.string.add_first_transaction),
            onClick = onNavigateToAdd,
            modifier = Modifier.padding(horizontal = Dimens.SpacingXxl),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionRow(
    transaction: Transaction,
    category: Category?,
    currencyCode: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { fullWidth -> fullWidth * 0.35f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = colors.error,
                    modifier = Modifier.padding(end = Dimens.SpacingL),
                )
            }
        },
    ) {
        TransactionRow(
            transaction = transaction,
            category = category,
            currencyCode = currencyCode,
            onClick = onClick,
            modifier = Modifier.background(colors.surface),
        )
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    leadingEmoji: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shape = RoundedCornerShape(Dimens.RadiusPill)
    val backgroundColor = if (selected) {
        selectedColor.copy(alpha = 0.15f)
    } else {
        colors.surfaceVariant
    }
    val borderModifier = if (selected) {
        Modifier.border(Dimens.borderThin, selectedColor, shape)
    } else {
        Modifier
    }
    val textColor = if (selected) selectedColor else colors.onSurfaceVariant

    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .then(borderModifier)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.SpacingS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXxs),
    ) {
        if (leadingEmoji != null) {
            Text(text = leadingEmoji, style = typography.bodySmall)
        }
        Text(
            text = label,
            style = typography.bodySmall,
            color = textColor,
        )
    }
}

private fun matchesTypeFilter(transaction: Transaction, filter: HistoryFilter): Boolean =
    when (filter) {
        HistoryFilter.ALL -> true
        HistoryFilter.INCOME -> transaction.type == TransactionType.INCOME ||
            transaction.type == TransactionType.SAVINGS
        HistoryFilter.EXPENSE -> transaction.type == TransactionType.EXPENSE
    }

private fun matchesCategoryFilter(transaction: Transaction, selectedCategoryIds: Set<Long>): Boolean =
    selectedCategoryIds.isEmpty() || transaction.categoryId in selectedCategoryIds

private fun matchesDateRange(
    transaction: Transaction,
    start: LocalDate?,
    end: LocalDate?,
): Boolean {
    if (start == null || end == null) return true
    val date = Instant.ofEpochMilli(transaction.dateMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    return !date.isBefore(start) && !date.isAfter(end)
}

@Composable
private fun formatDateHeader(timestamp: Long): String {
    val date = Date(timestamp)
    val dayKey = SimpleDateFormat("yyyyMMdd", Locale.US)
    val today = dayKey.format(Date())
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time.let(dayKey::format)
    val value = dayKey.format(date)
    return when (value) {
        today -> stringResource(R.string.today).replaceFirstChar { it.titlecase(Locale.getDefault()) }
        yesterday -> stringResource(R.string.yesterday).replaceFirstChar { it.titlecase(Locale.getDefault()) }
        else -> SimpleDateFormat("d MMMM", Locale("ru")).format(date)
    }
}

private fun categoryColor(
    category: Category,
    colors: androidx.compose.material3.ColorScheme,
): Color = category.colorArgb?.let { Color(it.toInt()) } ?: colors.primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transaction: Transaction,
    category: Category?,
    currencyCode: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val dateLabel =
        SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(Date(transaction.dateMillis))
    val amountColor = if (transaction.type == TransactionType.INCOME) colors.secondary else colors.error
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "–"
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingS + Dimens.SpacingXxs, vertical = Dimens.SpacingS),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.avatarSizeDetail)
                    .background(amountColor.copy(alpha = 0.12f), androidx.compose.foundation.shape.CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = category?.icon ?: "•", style = typography.headlineMedium)
            }
            Text(
                text = category?.name ?: stringResource(R.string.transaction_default),
                style = typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = "$amountPrefix${MoneyFormat.format(transaction.amountMinor, currencyCode)}",
                style = typography.displayLarge,
                color = amountColor,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            AppCard {
                Column(modifier = Modifier.padding(Dimens.SpacingXl)) {
                    DetailRow(stringResource(R.string.transaction_detail_date), dateLabel)
                }
            }
            AppCard {
                Column(modifier = Modifier.padding(Dimens.SpacingXl)) {
                    DetailRow(
                        stringResource(R.string.transaction_detail_category),
                        category?.name ?: stringResource(R.string.dash_placeholder),
                    )
                    HorizontalDivider(color = colors.outline)
                    DetailRow(
                        stringResource(R.string.transaction_detail_note),
                        transaction.note ?: stringResource(R.string.dash_placeholder),
                    )
                }
            }
            IosTextButton(
                text = stringResource(R.string.delete),
                onClick = onDelete,
                color = colors.error,
                style = typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(vertical = Dimens.spacingRow),
            )
            IosTextButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                color = colors.onSurfaceVariant,
                style = typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = PaddingValues(bottom = Dimens.SpacingXs),
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacingRow),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = typography.bodyMedium,
        )
    }
}
