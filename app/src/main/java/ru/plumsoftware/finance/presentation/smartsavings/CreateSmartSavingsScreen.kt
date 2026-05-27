package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
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
        if (state.saved) onCreatedUpdated() // Это закроет экран!
    }

    state.errorMessage?.let { msg ->
        IosAlertDialog(message = msg, onDismiss = viewModel::clearError)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // iOS фон: Светло-серый в светлой теме, глубокий черный в темной
        containerColor = if (isSystemInDarkTheme()) Color.Black else Color(0xFFF2F2F7),
        topBar = {
            IosTopBar(title = stringResource(R.string.smart_create_title), onBack = onBack)
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
                    .padding(horizontal = Dimens.paddingMedium),
            ) {
                Spacer(Modifier.height(Dimens.paddingLarge))

                // === БЛОК 1: Основная информация (iOS Grouped Style) ===
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.cornerRadiusCard))
                        .background(MaterialTheme.colorScheme.surface) // Белая карточка
                ) {
                    IosFormTextField(
                        label = "Название",
                        value = state.name,
                        onValueChange = viewModel::setName,
                        placeholder = "Напр. Термокружка"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(start = 16.dp))
                    IosFormTextField(
                        label = "Заметка",
                        value = state.note,
                        onValueChange = viewModel::setNote,
                        placeholder = "Необязательно"
                    )
                }

                Spacer(Modifier.height(Dimens.paddingLarge))

                // === БЛОК 2: Выбор иконки ===
                Text(
                    text = "ИКОНКА",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SMART_EMOJI_PRESETS.forEach { emoji ->
                        val selected = state.icon == emoji
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (selected) IosBlue else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.setIcon(emoji) }
                        ) {
                            Text(text = emoji, fontSize = 28.sp)
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.paddingLarge))

                // === БЛОК 3: Финансы ===
                Text(
                    text = "ФИНАНСЫ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.cornerRadiusCard))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    IosAmountSelectorRow(
                        label = stringResource(R.string.smart_purchase_cost),
                        digits = state.purchaseDigits,
                        currencyCode = state.currencyCode,
                        selected = activeField == AmountField.PURCHASE.ordinal,
                        onClick = { activeField = AmountField.PURCHASE.ordinal }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(start = 16.dp))
                    IosAmountSelectorRow(
                        label = stringResource(R.string.smart_saving_per_use),
                        digits = state.savingPerUseDigits,
                        currencyCode = state.currencyCode,
                        selected = activeField == AmountField.SAVING.ordinal,
                        onClick = { activeField = AmountField.SAVING.ordinal }
                    )
                }

                Spacer(Modifier.height(Dimens.paddingLarge))

                // === БЛОК 4: Настройки ===
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.cornerRadiusCard))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.smart_record_purchase),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    IosSwitch( // Используйте ваш IosSwitch
                        checked = state.recordPurchaseExpense,
                        onCheckedChange = viewModel::setRecordPurchaseExpense,
                    )
                }
                Spacer(Modifier.height(Dimens.paddingLarge))
            }

            // Калькулятор (NumPad) внизу
            FinanceNumPad(
                onDigit = { d -> if (activeField == AmountField.PURCHASE.ordinal) viewModel.appendPurchaseDigit(d) else viewModel.appendSavingDigit(d) },
                onBackspace = { if (activeField == AmountField.PURCHASE.ordinal) viewModel.backspacePurchase() else viewModel.backspaceSaving() },
                modifier = Modifier.padding(horizontal = Dimens.paddingLarge),
            )

            Spacer(Modifier.height(Dimens.paddingMedium))

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

// === ВСПОМОГАТЕЛЬНЫЕ UI-КОМПОНЕНТЫ iOS ===
@Composable
private fun IosFormTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.35f))
        Box(modifier = Modifier.weight(0.65f)) {
            if (value.isEmpty()) Text(text = placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), style = MaterialTheme.typography.bodyLarge)
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun IosAmountSelectorRow(label: String, digits: String, currencyCode: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) IosBlue.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = MoneyFormat.formatEntryDisplay(digits, currencyCode),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) IosBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
