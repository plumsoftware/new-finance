package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.IosCard
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.components.ios.IosTopBar
import ru.plumsoftware.finance.ui.theme.MascotAssets
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSavingsScreen(
    onCreateClick: () -> Unit,
    onAssetClick: (Long) -> Unit,
    snackbarMessage: String? = null,
    onSnackbarShown: () -> Unit = {},
    viewModel: SmartSavingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val allAssets = state.payingOff + state.profit
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHost.showSnackbar(msg)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            IosTopBar(
                title = stringResource(R.string.smart_savings_title),
                actions = {
                    IconButton(onClick = onCreateClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.smart_create_title),
                            tint = IosBlue,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = IosBlue,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
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
                Text(
                    text = stringResource(R.string.smart_savings_title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = Dimens.paddingMedium)
                )
            }
            item {
                IosCard {
                    Text(
                        stringResource(R.string.total_saved),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Text(
                        MoneyFormat.format(state.totalSavedMinor, state.currencyCode),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = IosGreen,
                    )
                    Text(
                        stringResource(R.string.smart_list_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = Dimens.paddingSmall),
                    )
                }
            }
            if (allAssets.isEmpty()) {
                item {
                    MascotEmptyState(
                        mascotRes = MascotAssets.emptySmartSavings,
                        title = stringResource(R.string.empty_smart_title),
                        subtitle = stringResource(R.string.empty_smart_subtitle),
                    )
                }
                item {
                    IosPrimaryButton(
                        text = stringResource(R.string.smart_add_button),
                        onClick = onCreateClick,
                    )
                }
            } else {
                items(allAssets, key = { it.id }) { asset ->
                    SmartAssetRow(
                        asset = asset,
                        currencyCode = state.currencyCode,
                        onClick = { onAssetClick(asset.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartAssetRow(
    asset: SmartAsset,
    currencyCode: String,
    onClick: () -> Unit,
) {
    IosCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = asset.icon,
                fontSize = 36.sp,
                modifier = Modifier.padding(end = Dimens.paddingMedium),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(
                        R.string.smart_saved_of,
                        MoneyFormat.format(asset.totalSavedMinor, currencyCode),
                        MoneyFormat.format(asset.purchaseCostMinor, currencyCode),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = IosGreen,
                )
                Spacer(Modifier.height(Dimens.paddingSmall))
                LinearProgressIndicator(
                    progress = { asset.paybackProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (asset.status == SmartAssetStatus.PROFIT) IosGreen else IosBlue,
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    text = if (asset.status == SmartAssetStatus.PROFIT) {
                        stringResource(R.string.smart_status_profit)
                    } else {
                        stringResource(R.string.smart_payback_percent, (asset.paybackProgress * 100).toInt())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
