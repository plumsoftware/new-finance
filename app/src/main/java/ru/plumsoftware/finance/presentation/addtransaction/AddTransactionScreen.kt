package ru.plumsoftware.finance.presentation.addtransaction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    viewModel: AddTransactionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    state.errorMessage?.let { message ->
        IosAlertDialog(
            message = message,
            onDismiss = viewModel::clearError,
        )
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
            }
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            IosTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                placeholder = stringResource(R.string.note_placeholder),
            )
            Spacer(modifier = Modifier.weight(1f))
            FinanceNumPad(
                onDigit = viewModel::appendDigit,
                onBackspace = viewModel::backspace,
            )
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
            IosPrimaryButton(
                text = stringResource(R.string.save),
                onClick = viewModel::save,
                loading = state.isSaving,
            )
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
        }
    }
}
