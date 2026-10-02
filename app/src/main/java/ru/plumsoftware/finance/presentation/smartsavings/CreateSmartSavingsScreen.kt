package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ads.InterstitialPlacement
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.dashboard.AmountEntrySheet
import ru.plumsoftware.finance.ui.ads.InterstitialAdEffect
import ru.plumsoftware.finance.ui.ds.BottomActionBar
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.EmojiPicker
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.FormSwitchRow
import ru.plumsoftware.finance.ui.ds.FormTextField
import ru.plumsoftware.finance.ui.ds.FormValueRow
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

private enum class AssetAmountField { PURCHASE, SAVING }

/**
 * Новый актив умной экономии (§6.10 п.4): название, заметка, иконка, стоимость, экономия за раз,
 * «Записать как расход». «Создать» закреплена внизу и неактивна, пока не заполнены название и суммы.
 */
@Composable
fun CreateSmartSavingsScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateSmartSavingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = FinanceTheme.colors
    val snackbar = LocalMascotSnackbar.current
    val onCreatedUpdated by rememberUpdatedState(onCreated)
    var amountField by rememberSaveable { mutableStateOf<AssetAmountField?>(null) }
    val purchaseMinor = Money.fromMajor(state.purchaseDigits.toDoubleOrNull() ?: 0.0, state.currencyCode)
    val savingMinor = Money.fromMajor(state.savingPerUseDigits.toDoubleOrNull() ?: 0.0, state.currencyCode)
    val canSave = state.name.isNotBlank() && purchaseMinor > 0 && savingMinor > 0 && !state.isSaving

    InterstitialAdEffect(
        placement = InterstitialPlacement.SMART_SAVINGS,
        trigger = state.saved,
        onContinue = { onCreatedUpdated() },
    )
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.show(it, Kopi.THINKING)
            viewModel.clearError()
        }
    }

    amountField?.let { field ->
        AmountEntrySheet(
            title = stringResource(if (field == AssetAmountField.PURCHASE) R.string.smart_purchase_cost_label else R.string.smart_saving_per_use),
            message = null,
            confirmLabel = stringResource(R.string.save),
            dismissLabel = stringResource(R.string.cancel),
            initialMinor = if (field == AssetAmountField.PURCHASE) purchaseMinor else savingMinor,
            currencyCode = state.currencyCode,
            onConfirm = { minor ->
                if (field == AssetAmountField.PURCHASE) viewModel.setPurchaseMinor(minor) else viewModel.setSavingMinor(minor)
                amountField = null
            },
            onDismiss = { amountField = null },
        )
    }

    Column(Modifier.fillMaxSize().background(c.bg).imePadding()) {
        SubScreenAppBar(
            title = stringResource(if (state.isEditMode) R.string.smart_edit_title else R.string.smart_new_asset),
            onBack = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(84.dp).background(c.success.copy(alpha = 0x22 / 255f), RoundedCornerShape(26.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(state.icon, fontSize = 40.sp) }
                VSpace(8.dp)
                Text(
                    state.name.ifBlank { stringResource(R.string.smart_name_placeholder) },
                    style = FinanceType.title,
                    color = if (state.name.isBlank()) c.textSecondary else c.textPrimary,
                )
                // Подсказка окупаемости, когда обе суммы заданы.
                if (purchaseMinor > 0 && savingMinor > 0) {
                    val uses = ((purchaseMinor + savingMinor - 1) / savingMinor).toInt()
                    Text(
                        pluralStringResource(R.plurals.pl_uses_left, uses, uses),
                        style = FinanceType.caption,
                        color = c.textSecondary,
                    )
                }
            }

            SectionHeader(stringResource(R.string.smart_section_main))
            FCard {
                FormTextField(state.name, viewModel::setName, stringResource(R.string.smart_name_placeholder))
                CardDivider(Modifier.padding(vertical = 8.dp))
                FormTextField(state.note, viewModel::setNote, stringResource(R.string.smart_note_optional))
            }

            SectionHeader(stringResource(R.string.smart_section_icon))
            FCard { EmojiPicker(SMART_EMOJI_PRESETS, state.icon, viewModel::setIcon) }

            SectionHeader(stringResource(R.string.smart_section_finance))
            FCard {
                FormValueRow(
                    stringResource(R.string.smart_purchase_cost_label),
                    if (purchaseMinor > 0) Money.formatRounded(purchaseMinor, state.currencyCode) else stringResource(R.string.period_choose),
                    onClick = { amountField = AssetAmountField.PURCHASE },
                    valueColor = if (purchaseMinor > 0) c.primary else c.textSecondary,
                )
                CardDivider()
                FormValueRow(
                    stringResource(R.string.smart_saving_per_use),
                    if (savingMinor > 0) Money.formatRounded(savingMinor, state.currencyCode) else stringResource(R.string.period_choose),
                    onClick = { amountField = AssetAmountField.SAVING },
                    valueColor = if (savingMinor > 0) c.primary else c.textSecondary,
                )
            }

            if (!state.isEditMode) {
                SectionHeader(stringResource(R.string.smart_section_settings))
                FCard {
                    FormSwitchRow(
                        stringResource(R.string.smart_record_as_expense),
                        state.recordPurchaseExpense,
                        hint = stringResource(R.string.smart_record_as_expense_hint),
                        onChange = viewModel::setRecordPurchaseExpense,
                    )
                }
            }
            VSpace(16.dp)
        }
        BottomActionBar(
            text = stringResource(if (state.isEditMode) R.string.save else R.string.create),
            onClick = viewModel::save,
            enabled = canSave,
        )
    }
}
