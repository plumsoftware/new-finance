package ru.plumsoftware.finance.presentation.history

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.MascotAssets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class HistoryGroup(
    val isToday: Boolean,
    val dateMillis: Long,
    val totalMinor: Long,
    val transactions: List<Transaction>,
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val groups = remember(state.transactions) {
        val keyFormatter = SimpleDateFormat("yyyyMMdd", Locale.US)
        val todayKey = keyFormatter.format(Date())
        state.transactions
            .groupBy { keyFormatter.format(Date(it.dateMillis)) }
            .toList()
            .sortedByDescending { it.first }
            .map { (key, items) ->
                val total = items.sumOf {
                    when (it.type) {
                        TransactionType.INCOME -> it.amountMinor
                        TransactionType.EXPENSE -> -it.amountMinor
                        TransactionType.SAVINGS -> 0L
                    }
                }
                HistoryGroup(
                    isToday = key == todayKey,
                    dateMillis = items.first().dateMillis,
                    totalMinor = total,
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
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.paddingMedium,
                end = Dimens.paddingMedium,
                top = Dimens.statusBarInset,
                bottom = Dimens.paddingMicro,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSection),
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.history_subtitle_90),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.history_title),
                        style = typography.headlineLarge,
                        color = colors.onSurface,
                    )
                }
            }
            if (groups.isEmpty()) {
                item {
                    MascotEmptyState(
                        mascotRes = MascotAssets.emptyTransactions,
                        title = stringResource(R.string.empty_transactions_title),
                        subtitle = stringResource(R.string.history_empty_period),
                    )
                }
            } else {
                groups.forEach { group ->
                    item {
                        val dayFormatter = SimpleDateFormat("d MMMM", Locale.getDefault())
                        val formattedDay = dayFormatter.format(Date(group.dateMillis)).uppercase()
                        val title = if (group.isToday) {
                            stringResource(R.string.history_today_header, formattedDay)
                        } else {
                            formattedDay
                        }
                        IosCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = title,
                                    style = typography.labelSmall,
                                    color = colors.onSurfaceVariant,
                                )
                                Text(
                                    text = MoneyFormat.format(
                                        group.totalMinor,
                                        state.currencyCode,
                                        showSign = true,
                                    ),
                                    style = typography.labelSmall,
                                    color = if (group.totalMinor >= 0) colors.tertiary else colors.error,
                                )
                            }
                            group.transactions.forEachIndexed { index, tx ->
                                if (index > 0) HorizontalDivider(color = colors.outline)
                                HistoryRow(
                                    transaction = tx,
                                    category = state.categoryMap[tx.categoryId],
                                    currencyCode = state.currencyCode,
                                    onClick = { selectedTransaction = tx },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    transaction: Transaction,
    category: Category?,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val date = SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).format(Date(transaction.dateMillis))
    val amountColor = if (transaction.type == TransactionType.INCOME) colors.tertiary else colors.error
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "–"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.spacingList),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.emojiPickerSize)
                .background(amountColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category?.icon ?: "•", style = typography.titleMedium)
        }
        Spacer(Modifier.size(Dimens.spacingList))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category?.name ?: (transaction.note ?: stringResource(R.string.transaction_default)),
                style = typography.titleMedium,
            )
            Text(
                text = date,
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix${MoneyFormat.format(transaction.amountMinor, currencyCode)}",
                style = typography.bodyMedium,
                color = amountColor,
            )
            Text(
                text = if (transaction.type == TransactionType.INCOME) {
                    stringResource(R.string.type_income)
                } else {
                    stringResource(R.string.type_expense)
                },
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

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
    val amountColor = if (transaction.type == TransactionType.INCOME) colors.tertiary else colors.error
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "–"
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingList + Dimens.paddingMicro, vertical = Dimens.spacingList),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.avatarSizeDetail)
                    .background(amountColor.copy(alpha = 0.12f), CircleShape)
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
            IosCard {
                DetailRow(stringResource(R.string.transaction_detail_date), dateLabel)
            }
            IosCard {
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
            IosTextButton(
                text = stringResource(R.string.delete),
                onClick = onDelete,
                color = colors.error,
                style = typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    vertical = Dimens.spacingRow,
                ),
            )
            IosTextButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                color = colors.onSurfaceVariant,
                style = typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = Dimens.paddingSmall,
                ),
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
