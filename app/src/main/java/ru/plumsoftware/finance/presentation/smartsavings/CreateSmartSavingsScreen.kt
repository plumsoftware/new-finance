package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.components.ios.IosTextField
import ru.plumsoftware.finance.ui.components.ios.IosTopBar
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue

private enum class AmountField { PURCHASE, SAVING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSmartSavingsScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateSmartSavingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeField by remember { mutableIntStateOf(0) }
    val onCreatedUpdated by rememberUpdatedState(onCreated)

    LaunchedEffect(state.saved) {
        if (state.saved) onCreatedUpdated()
    }

    state.errorMessage?.let { msg ->
        IosAlertDialog(message = msg, onDismiss = viewModel::clearError)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IosTopBar(
                title = stringResource(R.string.smart_create_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.paddingLarge),
            ) {
                Text(
                    stringResource(R.string.smart_create_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(Dimens.paddingLarge))
                IosTextField(
                    value = state.name,
                    onValueChange = viewModel::setName,
                    placeholder = stringResource(R.string.smart_name_example),
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                Text(
                    stringResource(R.string.smart_icon),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
                Spacer(Modifier.height(Dimens.paddingSmall))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                ) {
                    SMART_EMOJI_PRESETS.forEach { emoji ->
                        val selected = state.icon == emoji
                        Text(
                            text = emoji,
                            fontSize = 32.sp,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) IosBlue.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surface,
                                )
                                .clickable { viewModel.setIcon(emoji) }
                                .padding(10.dp),
                        )
                    }
                }
                Spacer(Modifier.height(Dimens.paddingLarge))
                AmountSelector(
                    label = stringResource(R.string.smart_purchase_cost),
                    digits = state.purchaseDigits,
                    currencyCode = state.currencyCode,
                    selected = activeField == AmountField.PURCHASE.ordinal,
                    onSelect = { activeField = AmountField.PURCHASE.ordinal },
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                AmountSelector(
                    label = stringResource(R.string.smart_saving_per_use),
                    digits = state.savingPerUseDigits,
                    currencyCode = state.currencyCode,
                    selected = activeField == AmountField.SAVING.ordinal,
                    onSelect = { activeField = AmountField.SAVING.ordinal },
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                IosTextField(
                    value = state.note,
                    onValueChange = viewModel::setNote,
                    placeholder = stringResource(R.string.smart_note_example),
                )
                Spacer(Modifier.height(Dimens.paddingMedium))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.smart_record_purchase),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    IosSwitch(
                        checked = state.recordPurchaseExpense,
                        onCheckedChange = viewModel::setRecordPurchaseExpense,
                    )
                }
                Spacer(Modifier.height(Dimens.paddingLarge))
            }
            FinanceNumPad(
                onDigit = { d ->
                    if (activeField == AmountField.PURCHASE.ordinal) {
                        viewModel.appendPurchaseDigit(d)
                    } else {
                        viewModel.appendSavingDigit(d)
                    }
                },
                onBackspace = {
                    if (activeField == AmountField.PURCHASE.ordinal) {
                        viewModel.backspacePurchase()
                    } else {
                        viewModel.backspaceSaving()
                    }
                },
                modifier = Modifier.padding(horizontal = Dimens.paddingLarge),
            )
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = Dimens.paddingLarge),
                )
                Spacer(Modifier.height(Dimens.paddingSmall))
            }
            IosPrimaryButton(
                text = stringResource(R.string.create),
                onClick = viewModel::save,
                loading = state.isSaving,
                modifier = Modifier.padding(horizontal = Dimens.paddingLarge),
            )
            Spacer(Modifier.height(Dimens.paddingLarge))
        }
    }
}

@Composable
private fun AmountSelector(
    label: String,
    digits: String,
    currencyCode: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) IosBlue.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface,
            )
            .clickable(onClick = onSelect)
            .padding(Dimens.paddingMedium),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        )
        Spacer(Modifier.height(Dimens.paddingSmall))
        Text(
            MoneyFormat.formatEntryDisplay(digits, currencyCode),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) IosBlue else MaterialTheme.colorScheme.onSurface,
        )
    }
}
