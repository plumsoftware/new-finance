package ru.plumsoftware.finance.presentation.smartsavings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.MascotEmptyState
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.components.ios.IosEditorTopBar
import ru.plumsoftware.finance.ui.components.ios.IosFilterChip
import ru.plumsoftware.finance.ui.components.ios.IosTextButton
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.MascotAssets

@OptIn(ExperimentalMaterial3Api::class)
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
    val displayedAssets = state.displayedAssets
    val snackbarHost = remember { SnackbarHostState() }
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            IosEditorTopBar(
                title = stringResource(R.string.smart_savings_block),
                backLabel = stringResource(R.string.nav_home),
                onBack = onBack,
                actionLabel = stringResource(R.string.categories_add),
                onAction = onCreateClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = Dimens.SpacingM,
                end = Dimens.SpacingM,
                top = Dimens.SpacingXxs,
                bottom = Dimens.SpacingXxs,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            item {
                HeroCard(totalSaved = MoneyFormat.format(state.totalSavedMinor, state.currencyCode))
            }
            item {
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenGoalsClick),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingS),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.goals_title),
                            style = typography.bodyLarge,
                            color = colors.onSurface,
                        )
                        IosTextButton(
                            text = stringResource(R.string.goals_add_button),
                            onClick = onOpenGoalsClick,
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.SpacingXxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionLabel(text = stringResource(R.string.smart_my_assets))
                    IosFilterChip(
                        text = stringResource(R.string.smart_filter_completed),
                        selected = state.showCompletedOnly,
                        onClick = viewModel::toggleCompletedFilter,
                    )
                }
            }

            if (displayedAssets.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.SpacingXxl + Dimens.SpacingS),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (state.showCompletedOnly) {
                            Text(
                                text = stringResource(R.string.smart_profit_empty),
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = Dimens.SpacingL),
                            )
                        } else {
                            MascotEmptyState(
                                mascotRes = MascotAssets.emptySmartSavings,
                                title = stringResource(R.string.smart_empty_assets_title),
                                subtitle = stringResource(R.string.smart_empty_assets_subtitle),
                            )
                            Spacer(Modifier.height(Dimens.SpacingXl))
                            IosPrimaryButton(
                                text = stringResource(R.string.smart_add_asset),
                                onClick = onCreateClick,
                                modifier = Modifier.padding(horizontal = Dimens.SpacingS),
                            )
                        }
                    }
                }
            } else {
                items(displayedAssets, key = { it.id }) { asset ->
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
private fun HeroCard(totalSaved: String) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingXl),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
        ) {
            Text(
                text = stringResource(R.string.smart_total_saved_header),
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = "+$totalSaved",
                style = typography.headlineSmall,
                color = colors.secondary,
            )
            HorizontalDivider(color = colors.outline, thickness = Dimens.dividerThickness)
            Text(
                text = stringResource(R.string.smart_hero_tip),
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SmartAssetRow(
    asset: SmartAsset,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val progress = asset.paybackProgress.coerceIn(0f, 1f)
    val isProfit = asset.status == SmartAssetStatus.PROFIT
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpacingM),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.emojiPickerSize)
                    .background(colors.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = asset.icon, style = typography.headlineMedium)
            }
            Spacer(Modifier.width(Dimens.SpacingS))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    style = typography.titleMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.smart_saved_of,
                        MoneyFormat.format(asset.totalSavedMinor, currencyCode),
                        MoneyFormat.format(asset.purchaseCostMinor, currencyCode),
                    ),
                    style = typography.labelMedium,
                    color = colors.secondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(Dimens.RadiusS))
                if (isProfit) {
                    Box(
                        modifier = Modifier
                            .background(
                                colors.secondary.copy(alpha = 0.12f),
                                MaterialTheme.shapes.extraSmall,
                            )
                            .padding(horizontal = Dimens.RadiusS, vertical = Dimens.SpacingXxs),
                    ) {
                        Text(
                            stringResource(R.string.smart_paid_off_badge),
                            style = typography.labelSmall,
                            color = colors.secondary,
                        )
                    }
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.progressHeightThin + 1.dp),
                        color = colors.primary,
                        trackColor = colors.outline,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        text = stringResource(R.string.percent_short, (progress * 100).toInt()),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens.SpacingXxs),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.outlineVariant,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(Dimens.dragIconSize),
            )
        }
    }
}
