package ru.plumsoftware.finance.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.history.TransactionDetailSheet
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onOpenSmartSavingsClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    onOpenAnalyticsClick: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearSnackbar()
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

    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF2F2F7),
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Добрый день 👋", fontSize = 15.sp, color = Color(0xFF8E8E93))
                        Text("Главная", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF2F2F7), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(20.dp),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 2.dp, end = 2.dp)
                                .size(8.dp)
                                .background(Color(0xFFFF3B30), CircleShape),
                        )
                    }
                }
            }
            item {
                HeroBalanceCard(
                    balanceMinor = state.totalBalanceMinor,
                    monthIncomeMinor = state.monthIncomeMinor,
                    monthExpenseMinor = state.monthExpenseMinor,
                    currencyCode = state.currencyCode,
                    onClick = onOpenAnalyticsClick,
                )
            }
            item {
                SectionHeader("Умная экономия", "Все →", onOpenSmartSavingsClick)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.smartAssets.isEmpty()) {
                        item { EmptySmartSavingsCard(onOpenSmartSavingsClick) }
                    } else {
                        items(state.smartAssets, key = { it.id }) { asset ->
                            SmartAssetDashboardCard(
                                asset = asset,
                                currencyCode = state.currencyCode,
                                onRecordUsage = { viewModel.recordSmartUsage(asset.id) },
                            )
                        }
                    }
                }
            }
            item {
                SectionHeader("Последние операции", "Все →", onOpenHistoryClick)
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.recentTransactions.isEmpty()) {
                        Text(
                            text = "Нет операций",
                            color = Color(0xFF8E8E93),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                        )
                    } else {
                        val recent = state.recentTransactions.take(5)
                        Column {
                            recent.forEachIndexed { index, tx ->
                                TransactionListRow(
                                    transaction = tx,
                                    category = state.categoryMap[tx.categoryId],
                                    currencyCode = state.currencyCode,
                                    onClick = { selectedTransaction = tx },
                                )
                                if (index != recent.lastIndex) {
                                    HorizontalDivider(
                                        color = Color(0xFFE5E5EA),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 60.dp),
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

@Composable
private fun HeroBalanceCard(
    balanceMinor: Long,
    monthIncomeMinor: Long,
    monthExpenseMinor: Long,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val monthLabel = SimpleDateFormat("LLLL yyyy", Locale("ru"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ОБЩИЙ БАЛАНС", fontSize = 12.sp, color = Color(0xFF8E8E93))
                Text("$monthLabel ▾", fontSize = 13.sp, color = IosBlue)
            }
            Text(
                text = MoneyFormat.format(balanceMinor, currencyCode),
                fontSize = 40.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        "↑ ${MoneyFormat.format(monthIncomeMinor, currencyCode, showSign = true)}",
                        color = IosGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Доходы", color = Color(0xFF8E8E93), fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "↓ ${MoneyFormat.format(monthExpenseMinor, currencyCode)}",
                        color = IosRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Расходы", color = Color(0xFF8E8E93), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Text(action, fontSize = 15.sp, color = IosBlue, modifier = Modifier.clickable(onClick = onActionClick))
    }
}

@Composable
private fun SmartAssetDashboardCard(
    asset: SmartAsset,
    currencyCode: String,
    onRecordUsage: () -> Unit,
) {
    val progress = asset.paybackProgress.coerceIn(0f, 1f)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.width(200.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${asset.icon} ${asset.name}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp),
                color = if (asset.status == SmartAssetStatus.PROFIT) IosGreen else IosBlue,
                trackColor = Color(0xFFE5E5EA),
                strokeCap = StrokeCap.Round,
            )
            Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFF8E8E93))
            Text("${MoneyFormat.format(asset.totalSavedMinor, currencyCode)} сэкономлено", fontSize = 13.sp, color = IosGreen)
            Button(
                onClick = onRecordUsage,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = IosBlue,
                ),
                border = BorderStroke(1.dp, IosBlue),
                modifier = Modifier.height(24.dp),
            ) {
                Text("+ Записать", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptySmartSavingsCard(onOpenSmartSavingsClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onOpenSmartSavingsClick),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("+ Добавить первый актив", color = IosBlue, fontSize = 15.sp)
            Text("Следи за окупаемостью", color = Color(0xFF8E8E93), fontSize = 12.sp)
        }
    }
}

@Composable
private fun TransactionListRow(
    transaction: Transaction,
    category: Category?,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val amountColor = if (transaction.type == TransactionType.INCOME) IosGreen else IosRed
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "−"
    val time = SimpleDateFormat("HH:mm", Locale("ru")).format(Date(transaction.dateMillis))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(amountColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category?.icon ?: "•", fontSize = 20.sp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = category?.name ?: (transaction.note ?: "Операция"),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("Счёт · $time", fontSize = 13.sp, color = Color(0xFF8E8E93))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix${MoneyFormat.format(transaction.amountMinor, currencyCode)}",
                color = amountColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(relativeDayLabel(transaction.dateMillis), fontSize = 12.sp, color = Color(0xFF8E8E93))
        }
    }
}

private fun relativeDayLabel(timestamp: Long): String {
    val date = Date(timestamp)
    val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.US)
    val today = dateFmt.format(Date())
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time.let(dateFmt::format)
    val value = dateFmt.format(date)
    return when (value) {
        today -> "сегодня"
        yesterday -> "вчера"
        else -> SimpleDateFormat("d MMM", Locale("ru")).format(date).lowercase()
    }
}
