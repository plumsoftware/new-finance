package ru.plumsoftware.finance.presentation.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.history.TransactionDetailSheet
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.MascotAssets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
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
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        LazyColumn(
            state = listState,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_greeting),
                            style = typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            style = typography.headlineLarge,
                            color = colors.onSurface,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(Dimens.notificationButton)
                            .background(colors.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(Dimens.iconSizeSmall),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = Dimens.cornerRadiusSegmentInner, end = Dimens.cornerRadiusSegmentInner)
                                .size(Dimens.notificationBadge)
                                .background(colors.error, CircleShape),
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
                SectionHeader(
                    title = stringResource(R.string.smart_savings_block),
                    action = stringResource(R.string.dashboard_see_all),
                    onActionClick = onOpenSmartSavingsClick,
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingList)) {
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
                SectionHeader(
                    title = stringResource(R.string.recent_transactions),
                    action = stringResource(R.string.dashboard_see_all),
                    onActionClick = onOpenHistoryClick,
                )
            }
            item {
                if (state.recentTransactions.isEmpty()) {
                    MascotEmptyState(
                        mascotRes = MascotAssets.emptyTransactions,
                        title = stringResource(R.string.empty_transactions_title),
                        subtitle = stringResource(R.string.empty_transactions_subtitle),
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(Dimens.cornerRadiusList),
                        color = colors.surface,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
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
                                        color = colors.outline,
                                        thickness = Dimens.dividerThickness,
                                        modifier = Modifier.padding(start = Dimens.transactionDividerInset),
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
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    val monthLabel = SimpleDateFormat("LLLL yyyy", Locale.forLanguageTag("ru"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }
    Card(
        onClick = onClick,
        shape = shapes.large,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationCard),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spacingSection),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingRow),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.dashboard_total_balance),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = "$monthLabel ▾",
                    style = typography.bodySmall,
                    color = colors.secondary,
                )
            }
            Text(
                text = MoneyFormat.format(balanceMinor, currencyCode),
                style = typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.onSurface,
            )
            HorizontalDivider(color = colors.outline, thickness = Dimens.dividerThickness)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "↑ ${MoneyFormat.format(monthIncomeMinor, currencyCode, showSign = true)}",
                        style = typography.bodyMedium,
                        color = colors.tertiary,
                    )
                    Text(
                        text = stringResource(R.string.income),
                        style = typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "↓ ${MoneyFormat.format(monthExpenseMinor, currencyCode)}",
                        style = typography.bodyMedium,
                        color = colors.error,
                    )
                    Text(
                        text = stringResource(R.string.expense),
                        style = typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String, onActionClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingMicro),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = typography.titleMedium,
            color = colors.onSurface,
        )
        IosTextButton(
            text = action,
            onClick = onActionClick,
            style = typography.bodyMedium,
        )
    }
}

@Composable
private fun SmartAssetDashboardCard(
    asset: SmartAsset,
    currencyCode: String,
    onRecordUsage: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val shapes = MaterialTheme.shapes
    val progress = asset.paybackProgress.coerceIn(0f, 1f)
    Surface(
        shape = RoundedCornerShape(Dimens.cornerRadiusList),
        color = colors.surface,
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .height(Dimens.smartCardHeight),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spacingList + Dimens.paddingMicro),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        ) {
            Text(
                text = "${asset.icon} ${asset.name}",
                style = typography.bodyMedium,
                maxLines = 1,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()               // Растянется ровно на ширину карточки
                    .height(Dimens.progressHeightThin),
                color = if (asset.status == SmartAssetStatus.PROFIT) colors.tertiary else colors.secondary,
                trackColor = colors.outline,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.dashboard_saved_amount,
                    MoneyFormat.format(asset.totalSavedMinor, currencyCode),
                ),
                style = typography.bodySmall,
                color = colors.tertiary,
            )
            Button(
                onClick = onRecordUsage,
                shape = shapes.extraSmall,
                contentPadding = PaddingValues(horizontal = Dimens.spacingRow, vertical = Dimens.cornerRadiusSegmentInner),
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = colors.secondary,
                ),
                border = BorderStroke(Dimens.borderThin, colors.secondary),
                modifier = Modifier.height(Dimens.buttonHeightPrimary),
            ) {
                Text(
                    text = stringResource(R.string.dashboard_record),
                    style = typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun EmptySmartSavingsCard(onOpenSmartSavingsClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Card(
        onClick = onOpenSmartSavingsClick,
        shape = RoundedCornerShape(Dimens.cornerRadiusList),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(),
        modifier = Modifier.wrapContentWidth(),
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(
                    horizontal = Dimens.paddingMedium,
                    vertical = Dimens.paddingLarge,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.plus_sign),
                style = typography.bodyMedium,
                color = colors.secondary,
            )
            Spacer(modifier = Modifier.width(Dimens.paddingSmall))
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMicro),
            ) {
                Text(
                    text = stringResource(R.string.dashboard_add_first_asset),
                    style = typography.bodyMedium,
                    color = colors.secondary,
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = stringResource(R.string.dashboard_track_payback),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                )
            }
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
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val amountColor = if (transaction.type == TransactionType.INCOME) colors.tertiary else colors.error
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "−"
    val time = SimpleDateFormat("HH:mm", Locale("ru")).format(Date(transaction.dateMillis))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.spacingList),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.avatarSize)
                .background(amountColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category?.icon ?: "•", style = typography.titleMedium)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.spacingList),
        ) {
            Text(
                text = category?.name ?: (transaction.note ?: stringResource(R.string.transaction_default)),
                style = typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.transaction_account_time, time),
                style = typography.bodySmall,
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
                text = relativeDayLabel(transaction.dateMillis),
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun relativeDayLabel(timestamp: Long): String {
    val date = Date(timestamp)
    val dateFmt = SimpleDateFormat("yyyyMMdd", Locale.US)
    val today = dateFmt.format(Date())
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time.let(dateFmt::format)
    val value = dateFmt.format(date)
    return when (value) {
        today -> stringResource(R.string.today)
        yesterday -> stringResource(R.string.yesterday)
        else -> SimpleDateFormat("d MMM", Locale("ru")).format(date).lowercase()
    }
}
