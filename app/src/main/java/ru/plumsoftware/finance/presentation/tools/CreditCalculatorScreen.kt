package ru.plumsoftware.finance.presentation.tools

import android.R.attr.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import java.util.*
import kotlin.math.pow

data class PaymentRow(
    val month: Int,
    val totalPayment: Double,
    val principalPart: Double,
    val interestPart: Double,
    val remainingDebt: Double
)

data class CreditUiState(
    val amount: String = "",
    val interestRate: String = "",
    val termMonths: String = "",
    val isAnnuity: Boolean = true,
    val monthlyPayment: Double = 0.0,
    val totalPaid: Double = 0.0,
    val overpayment: Double = 0.0,
    val schedule: List<PaymentRow> = emptyList(),
    val hasResult: Boolean = false
) {
    val canCalculate: Boolean
        get() = amount.cleanDouble() != null &&
                interestRate.cleanDouble() != null &&
                termMonths.cleanInt() != null
}

class CreditCalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CreditUiState())
        private set

    fun onAmountChanged(value: String) { state = state.copy(amount = value) }
    fun onRateChanged(value: String) { state = state.copy(interestRate = value) }
    fun onTermChanged(value: String) { state = state.copy(termMonths = value) }
    fun togglePaymentType(isAnnuity: Boolean) { state = state.copy(isAnnuity = isAnnuity) }

    fun calculate() {
        val p = state.amount.cleanDouble() ?: return
        val annualRate = state.interestRate.cleanDouble() ?: return
        val months = state.termMonths.cleanInt() ?: return

        val r = annualRate / 100.0 / 12.0

        if (state.isAnnuity) {
            val monthly = if (r == 0.0) p / months else p * r * (1 + r).pow(months) / ((1 + r).pow(months) - 1)
            val total = monthly * months
            val over = total - p

            var tempDebt = p
            val calculatedSchedule = (1..months).map { m ->
                val interest = tempDebt * r
                val principal = monthly - interest
                tempDebt = (tempDebt - principal).coerceAtLeast(0.0)
                PaymentRow(m, monthly, principal, interest, tempDebt)
            }

            state = state.copy(
                monthlyPayment = monthly,
                totalPaid = total,
                overpayment = over,
                schedule = calculatedSchedule,
                hasResult = true
            )
        } else {
            val principalPart = p / months
            var remaining = p
            var total = 0.0

            val calculatedSchedule = (1..months).map { m ->
                val interest = remaining * r
                val payment = principalPart + interest
                remaining = (remaining - principalPart).coerceAtLeast(0.0)
                total += payment
                PaymentRow(m, payment, principalPart, interest, remaining)
            }

            state = state.copy(
                monthlyPayment = calculatedSchedule.firstOrNull()?.totalPayment ?: 0.0,
                totalPaid = total,
                overpayment = total - p,
                schedule = calculatedSchedule,
                hasResult = true
            )
        }
    }
}

@Composable
fun CreditCalculatorScreen(
    onBack: () -> Unit,
    vm: CreditCalculatorViewModel = viewModel()
) {
    val uiState = vm.state
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = { ToolHeader(title = "Кредитный калькулятор", onBack = onBack) },
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
                            label = "Сумма кредита",
                            value = uiState.amount,
                            onValueChange = vm::onAmountChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Процентная ставка",
                            value = uiState.interestRate,
                            onValueChange = vm::onRateChanged,
                            placeholder = "0.0",
                            suffix = "%"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Срок",
                            value = uiState.termMonths,
                            onValueChange = vm::onTermChanged,
                            placeholder = "0",
                            suffix = "мес.",
                            isIntegerOnly = true
                        )
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Тип платежа", style = typography.bodyLarge, color = colors.onSurface)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(Dimens.RadiusPill))
                                    .background(colors.surfaceVariant),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                                        .background(if (uiState.isAnnuity) colors.primary else Color.Transparent)
                                        .clickable { vm.togglePaymentType(true) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Аннуит.", color = if (uiState.isAnnuity) Color.White else colors.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                                        .background(if (!uiState.isAnnuity) colors.primary else Color.Transparent)
                                        .clickable { vm.togglePaymentType(false) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Дифф.", color = if (!uiState.isAnnuity) Color.White else colors.onSurfaceVariant)
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
                            Text("Результаты расчета", style = typography.titleMedium, fontWeight = FontWeight.Bold)

                            Column {
                                Text(
                                    text = if (uiState.isAnnuity) "${formatOutput(uiState.monthlyPayment)} ₽"
                                    else "от ${formatOutput(uiState.monthlyPayment)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                                Text("Ежемесячный платеж", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.totalPaid)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text("Общая выплата", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.overpayment)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF3B30) // iOS-Style Red
                                )
                                Text("Переплата", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (uiState.hasResult && uiState.schedule.isNotEmpty()) {
                item {
                    Text(
                        "График платежей",
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = Dimens.SpacingS)
                    )
                }
                itemsIndexed(uiState.schedule) { index, row ->
                    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Месяц ${row.month}", style = typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${formatOutput(row.totalPayment)} ₽", style = typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                                Text(
                                    text = "Долг: ${formatOutput(row.principalPart)} ₽ | Проц: ${formatOutput(row.interestPart)} ₽",
                                    style = typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                        if (index < uiState.schedule.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(colors.surfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    }
}