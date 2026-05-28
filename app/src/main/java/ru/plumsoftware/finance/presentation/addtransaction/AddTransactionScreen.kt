package ru.plumsoftware.finance.presentation.addtransaction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.components.ios.IosChip
import ru.plumsoftware.finance.ui.components.ios.IosSegmentedControl
import ru.plumsoftware.finance.ui.components.ios.IosTextField
import ru.plumsoftware.finance.ui.components.ios.IosTopBar
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    viewModel: AddTransactionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val canSave = MoneyFormat.majorDigitsToMinor(state.amountMajorDigits, state.currencyCode) > 0L && !state.isSaving
    var showQuickCategorySheet by remember { mutableStateOf(false) }
    var quickName by remember { mutableStateOf("") }
    var quickIcon by remember { mutableStateOf("🛒") }
    var quickColor by remember { mutableLongStateOf(0xFFFF3B30) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onBack()
        }
    }

    state.errorMessage?.let { message ->
        IosAlertDialog(
            message = message,
            onDismiss = viewModel::clearError,
        )
    }

    if (showQuickCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickCategorySheet = false },
            containerColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("Новая категория", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                IosTextField(
                    value = quickName,
                    onValueChange = { quickName = it.take(30) },
                    placeholder = "Название",
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    val emojis = if (state.type == TransactionType.INCOME) {
                        listOf("💼", "💻", "📈", "🎓", "💰", "🏆", "🚀", "🎤")
                    } else {
                        listOf("🛒", "☕", "🚌", "💊", "🍕", "🏠", "🎮", "🎁")
                    }
                    emojis.forEach { emoji ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (quickIcon == emoji) Color(quickColor.toInt()).copy(alpha = 0.2f) else Color(0xFFF2F2F7),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { quickIcon = emoji },
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        0xFFFF3B30, 0xFFFF9500, 0xFF34C759, 0xFF007AFF, 0xFF5856D6,
                    ).forEach { color ->
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = Color(color.toInt()),
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { quickColor = color },
                        ) {
                            if (quickColor == color) {
                                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                IosPrimaryButton(
                    text = "Создать",
                    onClick = {
                        viewModel.createQuickCategory(
                            name = quickName,
                            icon = quickIcon,
                            colorArgb = quickColor,
                            onCreated = {
                                quickName = ""
                                showQuickCategorySheet = false
                            },
                        )
                    },
                    enabled = quickName.isNotBlank(),
                    loading = state.quickCategorySaving,
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IosTopBar(
                title = stringResource(R.string.add_transaction),
                onBack = onBack,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.paddingLarge)
                    .navigationBarsPadding()
                    .padding(bottom = Dimens.paddingLarge),
            ) {
                IosPrimaryButton(
                    text = stringResource(R.string.save),
                    onClick = viewModel::save,
                    loading = state.isSaving,
                    enabled = canSave,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.paddingLarge),
        ) {
            val segmentType = when (state.type) {
                TransactionType.INCOME -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }
            IosSegmentedControl(
                labels = listOf(
                    stringResource(R.string.type_expense),
                    stringResource(R.string.type_income),
                ),
                selectedIndex = if (segmentType == TransactionType.INCOME) 1 else 0,
                onSelectIndex = { index ->
                    viewModel.setType(
                        if (index == 1) TransactionType.INCOME else TransactionType.EXPENSE,
                    )
                },
            )
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            AnimatedContent(
                targetState = state.amountMajorDigits,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                },
                label = "amount",
            ) { digits ->
                Text(
                    text = MoneyFormat.formatEntryDisplay(digits, state.currencyCode),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
            ) {
                state.categories.forEach { cat ->
                    IosChip(
                        text = "${cat.icon} ${cat.name}",
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { viewModel.selectCategory(cat.id) },
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF2F2F7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7C7CC)),
                    modifier = Modifier.clickable { showQuickCategorySheet = true },
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("+", color = IosBlue, fontWeight = FontWeight.SemiBold)
                        Text(" Новая", color = IosBlue, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            IosTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                placeholder = stringResource(R.string.note_placeholder),
            )
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            FinanceNumPad(
                onDigit = viewModel::appendDigit,
                onBackspace = viewModel::backspace,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
