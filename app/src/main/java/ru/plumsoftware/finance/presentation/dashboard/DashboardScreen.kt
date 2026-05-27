package ru.plumsoftware.finance.presentation.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.transactions.TransactionRow
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.theme.MascotAssets
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val listState = rememberSaveable(saver = androidx.compose.foundation.lazy.LazyListState.Saver) {
        androidx.compose.foundation.lazy.LazyListState()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Dimens.paddingLarge,
                end = Dimens.paddingLarge,
                top = Dimens.paddingSmall,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge),
        ) {
            item {
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                Text(
                    text = MoneyFormat.format(state.totalBalanceMinor, state.currencyCode),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
                ) {
                    SummaryChip(
                        label = stringResource(R.string.income),
                        amount = state.todayIncomeMinor,
                        currency = state.currencyCode,
                        positive = true,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryChip(
                        label = stringResource(R.string.expense),
                        amount = state.todayExpenseMinor,
                        currency = state.currencyCode,
                        positive = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (state.smartAssets.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.smart_savings_block),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)) {
                        items(state.smartAssets, key = { it.id }) { asset ->
                            SmartAssetDashboardCard(
                                asset = asset,
                                currencyCode = state.currencyCode,
                                onRecordUsage = { viewModel.recordSmartUsage(asset.id) },
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = stringResource(R.string.dashboard_smart_empty_short),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }
            item {
                Text(
                    text = stringResource(R.string.recent_transactions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (state.recentTransactions.isEmpty()) {
                item {
                    MascotEmptyState(
                        mascotRes = MascotAssets.emptyTransactions,
                        title = stringResource(R.string.empty_transactions_title),
                        subtitle = stringResource(R.string.empty_transactions_subtitle),
                    )
                }
            } else {
                items(state.recentTransactions, key = { it.id }) { tx ->
                    TransactionRow(
                        transaction = tx,
                        currencyCode = state.currencyCode,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    amount: Long,
    currency: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    IosCard(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        Text(
            text = MoneyFormat.format(amount, currency, showSign = positive),
            style = MaterialTheme.typography.titleMedium,
            color = if (positive) IosGreen else IosRed,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SmartAssetDashboardCard(
    asset: SmartAsset,
    currencyCode: String,
    onRecordUsage: () -> Unit,
) {
    IosCard(modifier = Modifier.fillMaxWidth(0.85f)) {
        Text(text = "${asset.icon} ${asset.name}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        val progress = asset.paybackProgress
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (asset.status == SmartAssetStatus.PROFIT) IosGreen else IosBlue,
            strokeCap = StrokeCap.Round,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingSmall))
        Text(
            text = if (asset.status == SmartAssetStatus.PROFIT) {
                stringResource(R.string.smart_status_profit)
            } else {
                stringResource(R.string.smart_payback_percent, (progress * 100).toInt())
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
        Text(
            text = MoneyFormat.format(asset.totalSavedMinor, currencyCode),
            style = MaterialTheme.typography.bodyLarge,
            color = IosGreen,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))
        TextButton(onClick = onRecordUsage) {
            Text(
                text = stringResource(R.string.smart_record_usage),
                color = IosBlue,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
