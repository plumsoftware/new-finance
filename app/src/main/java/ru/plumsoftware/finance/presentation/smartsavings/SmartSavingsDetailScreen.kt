package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.dashboard.AmountEntrySheet
import ru.plumsoftware.finance.ui.ds.ButtonPrimary
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmptyState
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FProgressBar
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.IconButton44
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.SectionTitle
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.TextAction
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.ds.masked
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Подробности актива умной экономии: окупаемость, отметка использования, история, удаление. */
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
    val c = FinanceTheme.colors
    val snackbar = LocalMascotSnackbar.current
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var customAmount by rememberSaveable { mutableStateOf(false) }

    // Данные перечитываются при возврате на экран, например после редактирования.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        var first = true
        val observer = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) {
                if (!first) viewModel.refresh()
                first = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.show(it, Kopi.THINKING)
            viewModel.clearError()
        }
    }

    if (customAmount && asset != null) {
        AmountEntrySheet(
            title = stringResource(R.string.smart_record_usage_title),
            message = stringResource(R.string.smart_custom_amount_hint),
            confirmLabel = stringResource(R.string.save),
            dismissLabel = stringResource(R.string.cancel),
            initialMinor = asset.alternativeCostMinor,
            currencyCode = state.currencyCode,
            onConfirm = {
                viewModel.recordAmount(it)
                customAmount = false
            },
            onDismiss = { customAmount = false },
        )
    }
    if (confirmDelete && asset != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = c.surface,
            title = { Text(stringResource(R.string.smart_delete_asset_title, asset.name), style = FinanceType.titleLarge, color = c.textPrimary) },
            text = { Text(stringResource(R.string.smart_delete_text), style = FinanceType.bodySmall, color = c.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteAsset(onDeleteSuccess)
                }) { Text(stringResource(R.string.delete), color = c.dangerText) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel), color = c.textSecondary) } },
        )
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(
            title = asset?.name.orEmpty(),
            onBack = onBack,
            actions = { if (asset != null) IconButton44(R.drawable.ic_edit, stringResource(R.string.cd_edit), { onEdit(asset.id) }) },
        )
        if (asset == null) {
            EmptyState(title = stringResource(R.string.smart_asset_not_found), pose = Kopi.THINKING, modifier = Modifier.padding(top = 48.dp))
            return@Column
        }
        val cur = state.currencyCode
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Окупаемость.
            FCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(64.dp).background(c.success.copy(alpha = 0x22 / 255f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Text(asset.icon, fontSize = 30.sp) }
                    HSpace(14.dp)
                    Column(Modifier.weight(1f)) {
                        Text(asset.name, style = FinanceType.titleSection, color = c.textPrimary)
                        if (!asset.note.isNullOrBlank()) Text(asset.note, style = FinanceType.caption, color = c.textSecondary)
                    }
                    Text(
                        "${(asset.paybackProgress * 100).toInt()}%",
                        style = FinanceType.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (asset.isPaidOff) c.successText else c.primary,
                    )
                }
                VSpace(14.dp)
                FProgressBar(asset.paybackProgress, if (asset.isPaidOff) c.success else c.primary, height = 8.dp)
                VSpace(8.dp)
                Text(
                    stringResource(
                        R.string.smart_progress_of,
                        masked(Money.formatRounded(asset.totalSavedMinor.coerceAtMost(asset.purchaseCostMinor), cur)),
                        Money.formatRounded(asset.purchaseCostMinor, cur),
                    ),
                    style = FinanceType.bodySmall,
                    color = c.textSecondary,
                )
                val usesLeft = if (asset.alternativeCostMinor > 0) {
                    ((asset.purchaseCostMinor - asset.totalSavedMinor + asset.alternativeCostMinor - 1) / asset.alternativeCostMinor).toInt()
                } else 0
                Text(
                    if (asset.isPaidOff) stringResource(R.string.smart_paid_off_status)
                    else pluralStringResource(R.plurals.pl_uses_left, usesLeft, usesLeft),
                    style = FinanceType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (asset.isPaidOff) c.successText else c.textPrimary,
                )
            }

            // Показатели.
            FCard {
                Row(Modifier.fillMaxWidth()) {
                    Stat(stringResource(R.string.smart_stat_saved), masked(Money.withSign(asset.totalSavedMinor, cur)), c.successText, Modifier.weight(1f))
                    Stat(stringResource(R.string.smart_stat_uses), asset.totalUses.toString(), c.textPrimary, Modifier.weight(1f))
                    Stat(stringResource(R.string.smart_stat_per_use), Money.formatRounded(asset.alternativeCostMinor, cur), c.textPrimary, Modifier.weight(1f))
                }
            }

            // Отметить использование.
            ButtonPrimary(
                text = stringResource(R.string.smart_mark_use, Money.formatRounded(asset.alternativeCostMinor, cur)),
                onClick = { viewModel.recordAmount(asset.alternativeCostMinor) },
                enabled = !state.isSaving,
                color = c.success,
                modifier = Modifier.fillMaxWidth(),
            )
            TextAction(
                stringResource(R.string.smart_other_amount),
                { customAmount = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            // История.
            FCard {
                SectionTitle(stringResource(R.string.smart_history_title))
                if (state.usages.isEmpty()) {
                    VSpace(8.dp)
                    Text(stringResource(R.string.smart_history_empty_short), style = FinanceType.bodySmall, color = c.textSecondary)
                } else {
                    state.usages.forEachIndexed { i, u ->
                        if (i > 0) CardDivider()
                        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                            val d = DateFmt.toLocalDate(u.usedAtMillis)
                            Column(Modifier.weight(1f)) {
                                Text(DateFmt.dayMonth(d), style = FinanceType.body, color = c.textPrimary)
                                Text(DateFmt.time(u.usedAtMillis) + (u.note?.let { " · $it" } ?: ""), style = FinanceType.caption, color = c.textSecondary)
                            }
                            Text(masked(Money.withSign(u.savedAmountMinor, cur)), style = FinanceType.body.copy(fontWeight = FontWeight.SemiBold), color = c.successText)
                        }
                    }
                }
            }

            TextAction(
                stringResource(R.string.smart_delete_asset),
                { confirmDelete = true },
                color = c.dangerText,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            VSpace(16.dp)
        }
    }
}

@Composable
private fun Stat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = FinanceType.title.copy(fontWeight = FontWeight.Bold), color = color, textAlign = TextAlign.Center, maxLines = 1)
        Text(label, style = FinanceType.caption, color = FinanceTheme.colors.textSecondary, textAlign = TextAlign.Center)
    }
}
