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
import kotlin.math.pow

data class MortgageUiState(
    val propertyCost: String = "",
    val downPaymentPercent: String = "",
    val rate: String = "12.0",
    val termYears: String = "20",
    val isFamilyRate: Boolean = false,
    val loanAmount: Double = 0.0,
    val monthlyPayment: Double = 0.0,
    val totalPaid: Double = 0.0,
    val overpayment: Double = 0.0,
    val insuranceEstimate: Double = 0.0,
    val hasResult: Boolean = false
) {
    val canCalculate: Boolean
        get() = propertyCost.cleanDouble() != null &&
                downPaymentPercent.cleanDouble() != null &&
                termYears.cleanInt() != null
}

class MortgageCalculatorViewModel : ViewModel() {
    var state by mutableStateOf(MortgageUiState())
        private set

    fun onCostChanged(value: String) { state = state.copy(propertyCost = value) }
    fun onDownPaymentChanged(value: String) { state = state.copy(downPaymentPercent = value) }
    fun onRateChanged(value: String) { state = state.copy(rate = value) }
    fun onTermChanged(value: String) { state = state.copy(termYears = value) }
    fun toggleFamilyRate(isFamily: Boolean) {
        state = state.copy(
            isFamilyRate = isFamily,
            rate = if (isFamily) "6.0" else "12.0"
        )
    }

    fun calculate() {
        val cost = state.propertyCost.cleanDouble() ?: return
        val downPaymentPct = state.downPaymentPercent.cleanDouble() ?: return
        val years = state.termYears.cleanInt() ?: return

        val loan = cost * (1.0 - (downPaymentPct / 100.0))
        val annualRate = if (state.isFamilyRate) 6.0 else (state.rate.cleanDouble() ?: 12.0)
        val r = annualRate / 100.0 / 12.0
        val months = years * 12

        val monthly = if (r == 0.0) loan / months else loan * r * (1 + r).pow(months) / ((1 + r).pow(months) - 1)
        val total = monthly * months
        val insurance = loan * 0.003

        state = state.copy(
            loanAmount = loan,
            monthlyPayment = monthly,
            totalPaid = total,
            overpayment = total - loan,
            insuranceEstimate = insurance,
            hasResult = true
        )
    }
}

@Composable
fun MortgageCalculatorScreen(
    onBack: () -> Unit,
    vm: MortgageCalculatorViewModel = viewModel()
) {
    val uiState = vm.state
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = { ToolHeader(title = "Ипотечный калькулятор", onBack = onBack) },
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
                            label = "Стоимость недвижимости",
                            value = uiState.propertyCost,
                            onValueChange = vm::onCostChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Первоначальный взнос",
                            value = uiState.downPaymentPercent,
                            onValueChange = vm::onDownPaymentChanged,
                            placeholder = "0.0",
                            suffix = "%"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Срок",
                            value = uiState.termYears,
                            onValueChange = vm::onTermChanged,
                            placeholder = "0",
                            suffix = "лет",
                            isIntegerOnly = true
                        )
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Семейная ипотека (6%)", style = typography.bodyLarge, color = colors.onSurface)
                            Switch(
                                checked = uiState.isFamilyRate,
                                onCheckedChange = vm::toggleFamilyRate
                            )
                        }
                        if (!uiState.isFamilyRate) {
                            HorizontalDivider()
                            ToolInputField(
                                label = "Процентная ставка",
                                value = uiState.rate,
                                onValueChange = vm::onRateChanged,
                                placeholder = "12.0",
                                suffix = "%"
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
                            Text("Расчет ипотеки", style = typography.titleMedium, fontWeight = FontWeight.Bold)

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.loanAmount)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text("Сумма кредита", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.monthlyPayment)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                                Text("Ежемесячный платеж", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.overpayment)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF3B30)
                                )
                                Text("Переплата за весь срок", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.insuranceEstimate)} ₽/год",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurfaceVariant
                                )
                                Text("Годовая страховка (оценка)", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}