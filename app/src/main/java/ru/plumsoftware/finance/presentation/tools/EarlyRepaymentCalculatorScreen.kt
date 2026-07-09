package ru.plumsoftware.finance.presentation.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.theme.Dimens
import kotlin.math.log10
import kotlin.math.pow

data class EarlyRepayUiState(
    val loanAmount: String = "",
    val rate: String = "",
    val termMonths: String = "",
    val paidMonths: String = "",
    val extraPayment: String = "",
    val isReduceTerm: Boolean = true,
    val savedMonths: Int = 0,
    val savedInterest: Double = 0.0,
    val newPayment: Double = 0.0,
    val hasResult: Boolean = false
) {
    val canCalculate: Boolean
        get() = loanAmount.cleanDouble() != null &&
                rate.cleanDouble() != null &&
                termMonths.cleanInt() != null &&
                paidMonths.cleanInt() != null &&
                extraPayment.cleanDouble() != null
}

class EarlyRepaymentViewModel : ViewModel() {
    var state by mutableStateOf(EarlyRepayUiState())
        private set

    fun onLoanChanged(value: String) { state = state.copy(loanAmount = value) }
    fun onRateChanged(value: String) { state = state.copy(rate = value) }
    fun onTermChanged(value: String) { state = state.copy(termMonths = value) }
    fun onPaidChanged(value: String) { state = state.copy(paidMonths = value) }
    fun onExtraChanged(value: String) { state = state.copy(extraPayment = value) }
    fun setReductionMode(isReduceTerm: Boolean) { state = state.copy(isReduceTerm = isReduceTerm) }

    fun calculate() {
        val loan = state.loanAmount.cleanDouble() ?: return
        val annualRate = state.rate.cleanDouble() ?: return
        val totalMonths = state.termMonths.cleanInt() ?: return
        val paid = state.paidMonths.cleanInt() ?: return
        val extra = state.extraPayment.cleanDouble() ?: return

        val r = annualRate / 100.0 / 12.0
        val remainingMonths = totalMonths - paid

        val currentPayment = if (r == 0.0) loan / totalMonths else loan * r * (1 + r).pow(totalMonths) / ((1 + r).pow(totalMonths) - 1)

        var balance = loan
        repeat(paid) {
            val interest = balance * r
            val principal = currentPayment - interest
            balance = (balance - principal).coerceAtLeast(0.0)
        }

        val balanceBeforeExtra = balance
        val balanceAfterExtra = (balance - extra).coerceAtLeast(0.0)

        if (!state.isReduceTerm) {
            val newPayment = if (r == 0.0) balanceAfterExtra / remainingMonths else balanceAfterExtra * r * (1 + r).pow(remainingMonths) / ((1 + r).pow(remainingMonths) - 1)
            state = state.copy(
                newPayment = newPayment,
                savedInterest = (currentPayment - newPayment) * remainingMonths,
                savedMonths = 0,
                hasResult = true
            )
        } else {
            if (balanceAfterExtra > 0.0 && currentPayment > 0.0) {
                val newMonthsDouble = if (r == 0.0) balanceAfterExtra / currentPayment else log10(currentPayment / (currentPayment - balanceAfterExtra * r)) / log10(1 + r)
                val newMonths = kotlin.math.ceil(newMonthsDouble).toInt()
                val saved = remainingMonths - newMonths

                state = state.copy(
                    newPayment = currentPayment,
                    savedInterest = currentPayment * saved,
                    savedMonths = saved,
                    hasResult = true
                )
            }
        }
    }
}

@Composable
fun EarlyRepaymentCalculatorScreen(
    onBack: () -> Unit,
    vm: EarlyRepaymentViewModel = viewModel()
) {
    val uiState = vm.state
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = { ToolHeader(title = "Досрочное погашение", onBack = onBack) },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Dimens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
        ) {
            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                        ToolInputField(
                            label = "Остаток долга",
                            value = uiState.loanAmount,
                            onValueChange = vm::onLoanChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Процентная ставка",
                            value = uiState.rate,
                            onValueChange = vm::onRateChanged,
                            placeholder = "12.0",
                            suffix = "%"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Общий срок",
                            value = uiState.termMonths,
                            onValueChange = vm::onTermChanged,
                            placeholder = "0",
                            suffix = "мес.",
                            isIntegerOnly = true
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Уже выплачено месяцев",
                            value = uiState.paidMonths,
                            onValueChange = vm::onPaidChanged,
                            placeholder = "0",
                            suffix = "мес.",
                            isIntegerOnly = true
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Досрочный платеж",
                            value = uiState.extraPayment,
                            onValueChange = vm::onExtraChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Режим погашения", style = typography.bodyLarge, color = colors.onSurface)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.RadiusPill))
                                    .background(colors.surfaceVariant),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                                        .background(if (uiState.isReduceTerm) colors.primary else Color.Transparent)
                                        .clickable { vm.setReductionMode(true) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Срок", color = if (uiState.isReduceTerm) Color.White else colors.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                                        .background(if (!uiState.isReduceTerm) colors.primary else Color.Transparent)
                                        .clickable { vm.setReductionMode(false) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Платеж", color = if (!uiState.isReduceTerm) Color.White else colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = vm::calculate,
                    enabled = uiState.canCalculate,
                    modifier = Modifier.fillMaxWidth().height(Dimens.ButtonHeight),
                    shape = RoundedCornerShape(Dimens.RadiusL)
                ) {
                    Text("Рассчитать")
                }
            }

            item {
                AnimatedVisibility(visible = uiState.hasResult) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Dimens.SpacingM),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
                        ) {
                            Text("Расчет экономии", style = typography.titleMedium, fontWeight = FontWeight.Bold)

                            if (uiState.isReduceTerm) {
                                Column {
                                    Text(
                                        text = "${uiState.savedMonths} мес.",
                                        style = typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759) // iOS-Style Green
                                    )
                                    Text(
                                        text = "Сэкономлено времени",
                                        style = typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column {
                                    Text(
                                        text = "${formatOutput(uiState.newPayment)} ₽",
                                        style = typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759)
                                    )
                                    Text(
                                        text = "Новый ежемесячный платеж",
                                        style = typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.savedInterest)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF) // iOS-Style Blue
                                )
                                Text(
                                    text = "Сэкономлено на процентах",
                                    style = typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}