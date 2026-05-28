package ru.plumsoftware.finance.presentation.history

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed
import ru.plumsoftware.finance.ui.theme.MascotAssets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class HistoryGroup(
    val title: String,
    val totalMinor: Long,
    val transactions: List<Transaction>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val groups = remember(state.transactions) {
        val dayFormatter = SimpleDateFormat("d MMMM", Locale("ru"))
        val keyFormatter = SimpleDateFormat("yyyyMMdd", Locale.US)
        val todayKey = keyFormatter.format(Date())
        state.transactions
            .groupBy { keyFormatter.format(Date(it.dateMillis)) }
            .toList()
            .sortedByDescending { it.first }
            .map { (key, items) ->
                val title = if (key == todayKey) {
                    "СЕГОДНЯ, ${dayFormatter.format(Date(items.first().dateMillis)).uppercase()}"
                } else {
                    dayFormatter.format(Date(items.first().dateMillis)).uppercase()
                }
                val total = items.sumOf {
                    when (it.type) {
                        TransactionType.INCOME -> it.amountMinor
                        TransactionType.EXPENSE -> -it.amountMinor
                        TransactionType.SAVINGS -> 0L
                    }
                }
                HistoryGroup(title, total, items)
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
                    text = "История",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (groups.isEmpty()) {
                item {
                    MascotEmptyState(
                        mascotRes = MascotAssets.emptyTransactions,
                        title = stringResource(R.string.empty_transactions_title),
                        subtitle = "Нет операций за этот период",
                    )
                }
            } else {
                groups.forEach { group ->
                    item {
                        IosCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    group.title,
                                    color = Color(0xFF8E8E93),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    MoneyFormat.format(
                                        group.totalMinor,
                                        state.currencyCode,
                                        showSign = true
                                    ),
                                    color = if (group.totalMinor >= 0) IosGreen else IosRed,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            group.transactions.forEachIndexed { index, tx ->
                                if (index > 0) HorizontalDivider(color = Color(0xFFE5E5EA))
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
    val date = SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).format(Date(transaction.dateMillis))
    val amountColor = if (transaction.type == TransactionType.INCOME) IosGreen else IosRed
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "–"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    amountColor.copy(alpha = 0.1f),
                    androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category?.icon ?: "•")
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category?.name ?: (transaction.note ?: "Операция"), fontWeight = FontWeight.Bold)
            Text(date, color = Color(0xFF8E8E93))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$amountPrefix${MoneyFormat.format(transaction.amountMinor, currencyCode)}",
                color = amountColor,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                if (transaction.type == TransactionType.INCOME) "Доход" else "Расход",
                color = Color(0xFF8E8E93),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailSheet(
    transaction: Transaction,
    category: Category?,
    currencyCode: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateLabel =
        SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(Date(transaction.dateMillis))
    val amountColor = if (transaction.type == TransactionType.INCOME) IosGreen else IosRed
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "–"
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        amountColor.copy(alpha = 0.12f),
                        androidx.compose.foundation.shape.CircleShape
                    )
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = category?.icon ?: "•")
            }
            Text(
                text = category?.name ?: "Операция",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = "$amountPrefix${MoneyFormat.format(transaction.amountMinor, currencyCode)}",
                style = MaterialTheme.typography.displayLarge,
                color = amountColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            IosCard {
                DetailRow("📅 Дата и время", dateLabel)
            }
            IosCard {
                DetailRow("📂 Категория", category?.name ?: "–")
                HorizontalDivider(color = Color(0xFFE5E5EA))
                DetailRow("📝 Заметка", transaction.note ?: "–")
            }
            Text(
                text = "Удалить",
                color = IosRed,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDelete)
                    .padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                text = "Отмена",
                color = Color(0xFF8E8E93),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDismiss)
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Color(0xFF8E8E93))
        Text(value, fontWeight = FontWeight.Medium)
    }
}
