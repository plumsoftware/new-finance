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
import kotlin.math.ceil

data class GoalCalcUiState(
    val isAmountMode: Boolean = true,
    val targetAmount: String = "",
    val currentSaved: String = "",
    val periodMonths: String = "",
    val monthlyPayment: String = "",
    val calculatedMonthly: Double = 0.0,
    val calculatedMonths: Int = 0,
    val hasResult: Boolean = false
) {
    val canCalculate: Boolean
        get() = if (isAmountMode) {
            targetAmount.cleanDouble() != null && periodMonths.cleanInt() != null
        } else {
            targetAmount.cleanDouble() != null && monthlyPayment.cleanDouble() != null
        }
}

class GoalCalculatorViewModel : ViewModel() {
    var state by mutableStateOf(GoalCalcUiState())
        private set

    fun setMode(isAmountMode: Boolean) { state = state.copy(isAmountMode = isAmountMode, hasResult = false) }
    fun onTargetChanged(value: String) { state = state.copy(targetAmount = value) }
    fun onSavedChanged(value: String) { state = state.copy(currentSaved = value) }
    fun onPeriodChanged(value: String) { state = state.copy(periodMonths = value) }
    fun onMonthlyChanged(value: String) { state = state.copy(monthlyPayment = value) }

    fun calculate() {
        val target = state.targetAmount.cleanDouble() ?: return
        val saved = state.currentSaved.cleanDouble() ?: 0.0
        val remaining = (target - saved).coerceAtLeast(0.0)

        if (state.isAmountMode) {
            val months = state.periodMonths.cleanInt() ?: return
            state = state.copy(
                calculatedMonthly = if (months > 0) remaining / months else remaining,
                hasResult = true
            )
        } else {
            val monthly = state.monthlyPayment.cleanDouble() ?: return
            state = state.copy(
                calculatedMonths = if (monthly > 0) ceil(remaining / monthly).toInt() else 0,
                hasResult = true
            )
        }
    }
}

@Composable
fun GoalCalculatorScreen(
    onBack: () -> Unit,
    vm: GoalCalculatorViewModel = viewModel()
) {
    val uiState = vm.state
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = { ToolHeader(title = "Калькулятор цели", onBack = onBack) },
        containerColor = colors.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Dimens.SpacingL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                        .background(colors.surfaceVariant),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Dimens.RadiusPill))
                            .background(if (uiState.isAmountMode) colors.primary else Color.Transparent)
                            .clickable { vm.setMode(true) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Сколько откладывать?", color = if (uiState.isAmountMode) Color.White else colors.onSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Dimens.RadiusPill))
                            .background(if (!uiState.isAmountMode) colors.primary else Color.Transparent)
                            .clickable { vm.setMode(false) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("За сколько накоплю?", color = if (!uiState.isAmountMode) Color.White else colors.onSurfaceVariant)
                    }
                }
            }

            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Dimens.SpacingM)) {
                        ToolInputField(
                            label = "Целевая сумма",
                            value = uiState.targetAmount,
                            onValueChange = vm::onTargetChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Уже накоплено",
                            value = uiState.currentSaved,
                            onValueChange = vm::onSavedChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()

                        if (uiState.isAmountMode) {
                            ToolInputField(
                                label = "За какой срок",
                                value = uiState.periodMonths,
                                onValueChange = vm::onPeriodChanged,
                                placeholder = "0",
                                suffix = "мес.",
                                isIntegerOnly = true
                            )
                        } else {
                            ToolInputField(
                                label = "Сколько откладывать в месяц",
                                value = uiState.monthlyPayment,
                                onValueChange = vm::onMonthlyChanged,
                                placeholder = "0",
                                suffix = "₽"
                            )
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
                            Text("Прогноз цели", style = typography.titleMedium, fontWeight = FontWeight.Bold)

                            if (uiState.isAmountMode) {
                                Column {
                                    Text(
                                        text = "${formatOutput(uiState.calculatedMonthly)} ₽",
                                        style = typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF007AFF)
                                    )
                                    Text("Нужно сохранять в месяц", style = typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            } else {
                                Column {
                                    Text(
                                        text = "${uiState.calculatedMonths} мес.",
                                        style = typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34C759)
                                    )
                                    Text("Срок достижения", style = typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}