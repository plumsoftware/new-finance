package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetUsage
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.components.ios.IosNavigationTextButton
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
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
    var showDeleteSheet by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val listCardShape = RoundedCornerShape(Dimens.cornerRadiusList)

    state.errorMessage?.let { msg ->
        IosAlertDialog(message = msg, onDismiss = viewModel::clearError)
    }

    if (state.showRecordSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::closeRecordSheet,
            sheetState = sheetState,
            containerColor = colors.background,
        ) {
            var isNumPadVisible by remember { mutableStateOf(true) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge - 4.dp)
                    .padding(bottom = Dimens.bottomSheetBottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .width(Dimens.bottomSheetHandleWidth)
                        .height(Dimens.bottomSheetHandleHeight)
                        .background(colors.outlineVariant, RoundedCornerShape(Dimens.cornerRadiusHandle)),
                )
                Spacer(Modifier.height(Dimens.spacingRow))
                Text(
                    text = stringResource(R.string.smart_record_usage_title),
                    style = typography.titleLarge,
                )
                Spacer(Modifier.height(Dimens.spacingList))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = MoneyFormat.formatEntryDisplay(state.amountDigits, state.currencyCode),
                        style = typography.displayMedium,
                        color = colors.tertiary,
                        modifier = Modifier.clickable { isNumPadVisible = true },
                    )
                    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "cursor_alpha",
                    )
                    Text(
                        text = stringResource(R.string.pipe_separator),
                        style = typography.displayLarge,
                        color = colors.tertiary,
                        modifier = Modifier.alpha(cursorAlpha),
                    )
                }
                Text(
                    text = stringResource(R.string.smart_default_saving_hint),
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.paddingMicro + 2.dp, bottom = Dimens.paddingMedium),
                    textAlign = TextAlign.Center,
                )
                if (isNumPadVisible) {
                    FinanceNumPad(
                        onDigit = viewModel::appendDigit,
                        onBackspace = viewModel::backspace,
                        onCollapse = { isNumPadVisible = false },
                    )
                }
                Spacer(Modifier.height(Dimens.spacingRow + 4.dp))
                IosPrimaryButton(
                    text = stringResource(R.string.smart_confirm),
                    onClick = viewModel::recordSaving,
                    enabled = MoneyFormat.majorDigitsToMinor(state.amountDigits, state.currencyCode) > 0L,
                    loading = state.isSaving,
                )
            }
        }
    }

    if (showDeleteSheet && asset != null) {
        val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = deleteSheetState,
            containerColor = colors.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge - 4.dp, vertical = Dimens.paddingSmall),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.smart_delete_asset_title, asset.name),
                    style = typography.titleMedium,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(Dimens.paddingLarge - 6.dp))
                IosTextButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showDeleteSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = Dimens.spacingList,
                    ),
                )
                IosTextButton(
                    text = stringResource(R.string.delete),
                    onClick = {
                        showDeleteSheet = false
                        viewModel.deleteAsset(onDeleted = onDeleteSuccess)
                    },
                    color = colors.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        vertical = Dimens.spacingList,
                    ),
                )
                Spacer(Modifier.height(Dimens.paddingLarge - 6.dp))
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingSmall, vertical = Dimens.spacingRow),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IosNavigationTextButton(
                    text = stringResource(R.string.smart_savings_tab),
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.ArrowBackIosNew,
                )
                Text(
                    text = asset?.name.orEmpty(),
                    style = typography.bodyLarge,
                    color = colors.onSurface,
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = { onEdit(assetId) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.cd_edit),
                            tint = colors.secondary,
                            modifier = Modifier.size(Dimens.iconSizeSmall + 2.dp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (asset == null) {
            Text(
                text = stringResource(R.string.smart_asset_not_found),
                modifier = Modifier
                    .padding(padding)
                    .padding(Dimens.paddingLarge - 4.dp),
                style = typography.bodyLarge,
            )
            return@Scaffold
        }

        val animatedProgress by animateFloatAsState(
            targetValue = asset.paybackProgress.coerceIn(0f, 1f),
            animationSpec = tween(600),
            label = "asset_progress",
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = Dimens.paddingMedium, vertical = Dimens.spacingRow),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingList),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(Dimens.avatarSizeLarge)
                            .background(colors.secondary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(asset.icon, style = typography.displayMedium)
                    }
                    Spacer(Modifier.height(Dimens.spacingList))
                    Text(
                        text = asset.name,
                        style = typography.headlineMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "+${MoneyFormat.format(asset.totalSavedMinor, state.currencyCode)}",
                        style = typography.headlineSmall,
                        color = colors.tertiary,
                        modifier = Modifier.padding(top = Dimens.paddingMicro),
                    )
                    Text(
                        text = stringResource(R.string.smart_total_saved_label),
                        style = typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            item {
                Surface(
                    shape = listCardShape,
                    color = colors.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(Dimens.paddingMedium)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                stringResource(R.string.smart_payback_label),
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                            )
                            Text(
                                stringResource(R.string.percent_short, (animatedProgress * 100).toInt()),
                                style = typography.bodyMedium,
                                color = if (asset.status == SmartAssetStatus.PROFIT) colors.tertiary else colors.secondary,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Dimens.paddingSmall)
                                .height(Dimens.progressHeight),
                            color = if (asset.status == SmartAssetStatus.PROFIT) colors.tertiary else colors.secondary,
                            trackColor = colors.outline,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = if (asset.status == SmartAssetStatus.PROFIT) {
                                stringResource(R.string.smart_profit_celebration)
                            } else {
                                stringResource(
                                    R.string.smart_remaining_payback,
                                    MoneyFormat.format(
                                        (asset.purchaseCostMinor - asset.totalSavedMinor).coerceAtLeast(0),
                                        state.currencyCode,
                                    ),
                                )
                            },
                            style = typography.bodySmall,
                            color = if (asset.status == SmartAssetStatus.PROFIT) colors.tertiary else colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = Dimens.paddingSmall),
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingList),
                ) {
                    StatCell(
                        label = stringResource(R.string.smart_purchase_cost_label),
                        value = MoneyFormat.format(asset.purchaseCostMinor, state.currencyCode),
                        valueColor = colors.onSurface,
                        modifier = Modifier.weight(1f),
                        listCardShape = listCardShape,
                    )
                    StatCell(
                        label = stringResource(R.string.smart_saving_per_use),
                        value = MoneyFormat.format(asset.alternativeCostMinor, state.currencyCode),
                        valueColor = colors.tertiary,
                        modifier = Modifier.weight(1f),
                        listCardShape = listCardShape,
                    )
                }
            }
            item {
                IosPrimaryButton(
                    text = stringResource(R.string.smart_record_saving_cta),
                    onClick = viewModel::openRecordSheet,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            item {
                Text(
                    text = stringResource(R.string.smart_history_section),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = Dimens.paddingMicro, top = Dimens.paddingMicro),
                )
            }
            item {
                Surface(
                    shape = listCardShape,
                    color = colors.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.usages.isEmpty()) {
                        Text(
                            text = stringResource(R.string.smart_history_empty_short),
                            style = typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.paddingLarge),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Column {
                            state.usages.forEachIndexed { index, usage ->
                                UsageRow(usage, state.currencyCode)
                                if (index != state.usages.lastIndex) {
                                    HorizontalDivider(
                                        color = colors.outline,
                                        thickness = Dimens.dividerThickness,
                                        modifier = Modifier.padding(start = Dimens.spacingRow + 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                IosTextButton(
                    text = stringResource(R.string.smart_delete_asset),
                    onClick = { showDeleteSheet = true },
                    color = colors.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        top = Dimens.paddingMedium,
                        bottom = Dimens.bottomSheetBottomPadding,
                    ),
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    listCardShape: RoundedCornerShape,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Surface(
        shape = listCardShape,
        color = colors.surface,
        modifier = modifier,
    ) {
        Column(Modifier.padding(Dimens.spacingRow + 4.dp)) {
            Text(label, style = typography.labelSmall, color = colors.onSurfaceVariant)
            Text(
                value,
                style = typography.titleMedium,
                color = valueColor,
                modifier = Modifier.padding(top = Dimens.paddingMicro),
            )
        }
    }
}

@Composable
private fun UsageRow(usage: SmartAssetUsage, currencyCode: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val date = SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).format(Date(usage.usedAtMillis))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacingRow + 4.dp, vertical = Dimens.spacingRow + 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = date, style = typography.bodyMedium, color = colors.onSurfaceVariant)
        Text(
            text = "+${MoneyFormat.format(usage.savedAmountMinor, currencyCode)}",
            color = colors.tertiary,
            style = typography.bodyMedium,
        )
    }
}
