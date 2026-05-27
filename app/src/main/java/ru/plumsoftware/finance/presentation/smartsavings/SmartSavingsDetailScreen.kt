package ru.plumsoftware.finance.presentation.smartsavings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetUsage
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.components.ios.IosTopBar
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSavingsDetailScreen(
    assetId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: SmartSavingsDetailViewModel = koinViewModel { parametersOf(assetId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val asset = state.asset

    state.errorMessage?.let { msg ->
        IosAlertDialog(message = msg, onDismiss = viewModel::clearError)
    }

    if (state.showRecordSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        LaunchedEffect(Unit) { sheetState.expand() }
        ModalBottomSheet(
            onDismissRequest = viewModel::closeRecordSheet,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge)
                    .padding(bottom = Dimens.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.smart_record_amount_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                Text(
                    MoneyFormat.formatEntryDisplay(state.amountDigits, state.currencyCode),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = IosGreen,
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                FinanceNumPad(
                    onDigit = viewModel::appendDigit,
                    onBackspace = viewModel::backspace,
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                IosPrimaryButton(
                    text = stringResource(R.string.smart_record_confirm),
                    onClick = viewModel::recordSaving,
                    loading = state.isSaving,
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IosTopBar(
                title = asset?.name ?: "",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { onEdit(assetId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Изменить", tint = IosBlue)
                    }
                    IconButton(onClick = { viewModel.deleteAsset(onDeleted = onDeleteSuccess) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = IosRed)
                    }
                }
            )
        },
    ) { padding ->
        if (asset == null) {
            Text(
                stringResource(R.string.smart_not_found),
                modifier = Modifier.padding(padding).padding(Dimens.paddingLarge),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimens.paddingLarge,
                vertical = Dimens.paddingSmall,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(asset.icon, fontSize = 56.sp)
                    Spacer(Modifier.height(Dimens.paddingSmall))
                    Text(
                        MoneyFormat.format(asset.totalSavedMinor, state.currencyCode),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = IosGreen,
                    )
                    Text(
                        stringResource(R.string.smart_total_saved_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
            item {
                IosCard {
                    LinearProgressIndicator(
                        progress = { asset.paybackProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (asset.status == SmartAssetStatus.PROFIT) IosGreen else IosBlue,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(Modifier.height(Dimens.paddingSmall))
                    Text(
                        if (asset.status == SmartAssetStatus.PROFIT) {
                            stringResource(R.string.smart_status_profit)
                        } else {
                            stringResource(
                                R.string.smart_payback_percent,
                                (asset.paybackProgress * 100).toInt(),
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                ) {
                    StatCell(
                        label = stringResource(R.string.smart_stat_purchase),
                        value = MoneyFormat.format(asset.purchaseCostMinor, state.currencyCode),
                        modifier = Modifier.weight(1f),
                    )
                    StatCell(
                        label = stringResource(R.string.smart_stat_per_use),
                        value = MoneyFormat.format(asset.alternativeCostMinor, state.currencyCode),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                IosPrimaryButton(
                    text = stringResource(R.string.smart_record_usage),
                    onClick = viewModel::openRecordSheet,
                )
            }
            item {
                Text(
                    stringResource(R.string.smart_history),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (state.usages.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.smart_history_empty),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                items(state.usages, key = { it.id }) { usage ->
                    UsageRow(usage, state.currencyCode)
                }
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    IosCard(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun UsageRow(usage: SmartAssetUsage, currencyCode: String) {
    val date = SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(usage.usedAtMillis))
    IosCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(date, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(
                "+${MoneyFormat.format(usage.savedAmountMinor, currencyCode)}",
                color = IosGreen,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
