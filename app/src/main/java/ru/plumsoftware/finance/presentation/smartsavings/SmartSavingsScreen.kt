package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.ui.ds.EmojiBadge
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FProgressBar
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.IconButton44
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.KopiImage
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

private val UseBtnBg = Color(0xFFE3F7E8)
private val UseBtnText = Color(0xFF1D6B33)

@Composable
fun SmartSavingsScreen(
    onBack: () -> Unit,
    onCreateClick: () -> Unit,
    onOpenGoalsClick: () -> Unit,
    onAssetClick: (Long) -> Unit,
    snackbarMessage: String? = null,
    onSnackbarShown: () -> Unit = {},
    viewModel: SmartSavingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val snackbar = LocalMascotSnackbar.current
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbar.show(it, Kopi.HAPPY)
            onSnackbarShown()
        }
    }
    val assets = state.payingOff + state.profit
    val usageSaved = stringResource(R.string.smart_usage_saved)
    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(
            title = stringResource(R.string.smart_savings_title),
            onBack = onBack,
            actions = { IconButton44(R.drawable.ic_add, stringResource(R.string.smart_new_asset), onCreateClick) },
        )
        LazyColumn(
            Modifier.fillMaxSize().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.smart_total_saved), style = FinanceType.bodySmall, color = c.textSecondary)
                            Text(
                                Money.withSign(state.totalSavedMinor, state.currencyCode),
                                style = FinanceType.headlineLarge,
                                color = c.successText,
                            )
                            val uses = assets.sumOf { it.totalUses }
                            Text(
                                stringResource(R.string.smart_uses_instead, pluralStringResource(R.plurals.pl_uses, uses, uses)),
                                style = FinanceType.caption,
                                color = c.textSecondary,
                            )
                        }
                        KopiImage(Kopi.TROPHY, 72.dp)
                    }
                }
            }
            if (assets.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(R.string.smart_empty_assets_title),
                        text = stringResource(R.string.smart_empty_assets_subtitle),
                        action = stringResource(R.string.smart_new_asset),
                        onAction = onCreateClick,
                    )
                }
            }
            items(assets, key = { it.id }) { asset ->
                AssetCard(
                    asset = asset,
                    currency = state.currencyCode,
                    onClick = { onAssetClick(asset.id) },
                    onUse = {
                        viewModel.recordQuickUsage(asset.id)
                        snackbar.show(usageSaved, Kopi.HAPPY)
                    },
                )
            }
        }
    }
}

@Composable
private fun AssetCard(asset: SmartAsset, currency: String, onClick: () -> Unit, onUse: () -> Unit) {
    val c = FinanceTheme.colors
    FCard(radius = 22.dp, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge(asset.icon, c.success, size = 46.dp, circle = false)
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(asset.name, style = FinanceType.title, color = c.textPrimary, maxLines = 1)
                val base = stringResource(
                    R.string.smart_progress_of,
                    Money.formatRounded(asset.totalSavedMinor.coerceAtMost(asset.purchaseCostMinor), currency),
                    Money.formatRounded(asset.purchaseCostMinor, currency),
                )
                Text(
                    if (asset.note.isNullOrBlank()) base else "$base · ${asset.note}",
                    style = FinanceType.caption,
                    color = c.textSecondary,
                    maxLines = 2,
                )
            }
            HSpace(8.dp)
            Box(
                Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(UseBtnBg)
                    .clickable(role = Role.Button, onClick = onUse)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("+${Money.formatRounded(asset.alternativeCostMinor, currency)}", style = FinanceType.bodySmall.copy(fontWeight = FontWeight.Bold), color = UseBtnText)
            }
        }
        VSpace(12.dp)
        FProgressBar(asset.paybackProgress, if (asset.isPaidOff) c.success else c.primary, height = 5.dp)
        VSpace(6.dp)
        val usesLeft = if (asset.alternativeCostMinor > 0) {
            ((asset.purchaseCostMinor - asset.totalSavedMinor + asset.alternativeCostMinor - 1) / asset.alternativeCostMinor).toInt()
        } else 0
        Text(
            if (asset.isPaidOff) stringResource(R.string.smart_paid_off_status)
            else pluralStringResource(R.plurals.pl_uses_left, usesLeft, usesLeft),
            style = FinanceType.caption.copy(fontWeight = FontWeight.SemiBold),
            color = if (asset.isPaidOff) c.successText else c.textSecondary,
        )
    }
}
