package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.components.ios.IosSwitch
import ru.plumsoftware.finance.ui.theme.IosBlue

private enum class AmountField { PURCHASE, SAVING }
private val CreateBg = Color(0xFFF2F2F7)
private val CreateCard = Color.White
private val CreateSecondary = Color(0xFF8E8E93)
private val CreatePlaceholder = Color(0xFFC7C7CC)

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
    val canSave = state.name.isNotBlank() &&
        MoneyFormat.majorDigitsToMinor(state.purchaseDigits, state.currencyCode) > 0L

    LaunchedEffect(state.saved) {
        if (state.saved) onCreatedUpdated()
    }

    state.errorMessage?.let { msg ->
        IosAlertDialog(message = msg, onDismiss = viewModel::clearError)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CreateBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Отмена",
                    color = IosBlue,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onBack),
                )
                Text(
                    text = if (state.isEditMode) "Редактировать" else "Новый актив",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.weight(1f))
            }
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
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(56.dp))
                SectionTitle("ОСНОВНОЕ")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = CreateCard,
                ) {
                    Column {
                        IosFormTextField(
                            label = "Название",
                            value = state.name,
                            onValueChange = viewModel::setName,
                            placeholder = "Термокружка",
                        )
                        HorizontalDivider(color = Color(0xFFE5E5EA), modifier = Modifier.padding(start = 16.dp))
                        IosFormTextField(
                            label = "Заметка",
                            value = state.note,
                            onValueChange = viewModel::setNote,
                            placeholder = "Необязательно",
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionTitle("ИКОНКА")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SMART_EMOJI_PRESETS.forEach { emoji ->
                        val selected = state.icon == emoji
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (selected) IosBlue else Color(0xFFF2F2F7))
                                .clickable { viewModel.setIcon(emoji) },
                        ) {
                            Text(text = emoji, fontSize = 26.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionTitle("ФИНАНСЫ")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = CreateCard,
                ) {
                    Column {
                        IosAmountSelectorRow(
                            label = "Стоимость покупки",
                            digits = state.purchaseDigits,
                            currencyCode = state.currencyCode,
                            selected = activeField == AmountField.PURCHASE.ordinal,
                            onClick = { activeField = AmountField.PURCHASE.ordinal },
                        )
                        HorizontalDivider(color = Color(0xFFE5E5EA), modifier = Modifier.padding(start = 16.dp))
                        IosAmountSelectorRow(
                            label = "Экономия за раз",
                            digits = state.savingPerUseDigits,
                            currencyCode = state.currencyCode,
                            selected = activeField == AmountField.SAVING.ordinal,
                            onClick = { activeField = AmountField.SAVING.ordinal },
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                SectionTitle("НАСТРОЙКИ")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = CreateCard,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Записать как расход",
                            fontSize = 17.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f),
                        )
                        IosSwitch(
                            checked = state.recordPurchaseExpense,
                            onCheckedChange = viewModel::setRecordPurchaseExpense,
                        )
                    }
                }
                Text(
                    text = "При создании добавит транзакцию расхода",
                    fontSize = 12.sp,
                    color = CreateSecondary,
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                )
                Spacer(Modifier.height(16.dp))
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
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
            IosPrimaryButton(
                text = if (state.isEditMode) "Сохранить" else "Создать",
                onClick = viewModel::save,
                loading = state.isSaving,
                enabled = canSave,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(34.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = CreateSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun IosFormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            color = Color.Black,
            modifier = Modifier.weight(0.35f),
        )
        Box(modifier = Modifier.weight(0.65f), contentAlignment = Alignment.CenterEnd) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = CreatePlaceholder,
                    fontSize = 17.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.Black,
                    fontSize = 17.sp,
                    textAlign = TextAlign.End,
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

@Composable
private fun IosAmountSelectorRow(
    label: String,
    digits: String,
    currencyCode: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) IosBlue.copy(alpha = 0.06f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, fontSize = 17.sp, color = Color.Black)
        Text(
            text = MoneyFormat.formatEntryDisplay(digits, currencyCode),
            fontSize = 17.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) IosBlue else CreateSecondary,
        )
    }
}
