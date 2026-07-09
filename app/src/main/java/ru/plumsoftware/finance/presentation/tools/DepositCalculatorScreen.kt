package ru.plumsoftware.finance.presentation.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.theme.Dimens

data class DepositUiState(
    val initialAmount: String = "",
    val monthlyTopUp: String = "",
    val rate: String = "",
    val months: String = "",
    val capitalizationPeriod: Int = 1, // 1=Monthly, 3=Quarterly, 12=Yearly, 0=None
    val finalAmount: Double = 0.0,
    val totalDeposited: Double = 0.0,
    val income: Double = 0.0,
    val hasResult: Boolean = false
) {
    val canCalculate: Boolean
        get() = initialAmount.cleanDouble() != null &&
                rate.cleanDouble() != null &&
                months.cleanInt() != null
}

class DepositCalculatorViewModel : ViewModel() {
    var state by mutableStateOf(DepositUiState())
        private set

    fun onInitialAmountChanged(value: String) { state = state.copy(initialAmount = value) }
    fun onTopUpChanged(value: String) { state = state.copy(monthlyTopUp = value) }
    fun onRateChanged(value: String) { state = state.copy(rate = value) }
    fun onMonthsChanged(value: String) { state = state.copy(months = value) }
    fun setCapitalization(period: Int) { state = state.copy(capitalizationPeriod = period) }

    fun calculate() {
        val initial = state.initialAmount.cleanDouble() ?: return
        val topUp = state.monthlyTopUp.cleanDouble() ?: 0.0
        val annualRate = state.rate.cleanDouble() ?: return
        val term = state.months.cleanInt() ?: return
        val cap = state.capitalizationPeriod

        if (cap == 0) {
            val interest = initial * (annualRate / 100.0) * (term / 12.0)
            val deposited = initial + (topUp * term)
            state = state.copy(
                finalAmount = deposited + interest,
                totalDeposited = deposited,
                income = interest,
                hasResult = true
            )
        } else {
            val periodicRate = annualRate / 100.0 / (12.0 / cap)
            var balance = initial
            var totalDeposited = initial
            val totalPeriods = term / cap

            repeat(totalPeriods) {
                repeat(cap) {
                    balance += topUp
                    totalDeposited += topUp
                }
                balance *= (1 + periodicRate)
            }
            val remaining = term % cap
            repeat(remaining) {
                balance += topUp
                totalDeposited += topUp
            }

            state = state.copy(
                finalAmount = balance,
                totalDeposited = totalDeposited,
                income = balance - totalDeposited,
                hasResult = true
            )
        }
    }
}

@Composable
fun DepositCalculatorScreen(
    onBack: () -> Unit,
    vm: DepositCalculatorViewModel = viewModel()
) {
    val uiState = vm.state
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = { ToolHeader(title = "Калькулятор вклада", onBack = onBack) },
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
                            label = "Начальный взнос",
                            value = uiState.initialAmount,
                            onValueChange = vm::onInitialAmountChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Пополнение в месяц",
                            value = uiState.monthlyTopUp,
                            onValueChange = vm::onTopUpChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Ставка",
                            value = uiState.rate,
                            onValueChange = vm::onRateChanged,
                            placeholder = "0.0",
                            suffix = "%"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Срок",
                            value = uiState.months,
                            onValueChange = vm::onMonthsChanged,
                            placeholder = "0",
                            suffix = "мес.",
                            isIntegerOnly = true
                        )
                        HorizontalDivider()
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.SpacingS)) {
                            Text("Капитализация", style = typography.bodyLarge, color = colors.onSurface)
                            Spacer(Modifier.height(Dimens.SpacingXs))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.RadiusPill))
                                    .background(colors.surfaceVariant),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(1 to "Ежемес.", 3 to "Кварт.", 12 to "Ежегод.", 0 to "Нет").forEach { (valPeriod, label) ->
                                    val isSelected = uiState.capitalizationPeriod == valPeriod
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(Dimens.RadiusPill))
                                            .background(if (isSelected) colors.primary else Color.Transparent)
                                            .clickable { vm.setCapitalization(valPeriod) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = if (isSelected) Color.White else colors.onSurfaceVariant, maxLines = 1)
                                    }
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
                            Text("Результаты вклада", style = typography.titleMedium, fontWeight = FontWeight.Bold)

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.finalAmount)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34C759)
                                )
                                Text("Итоговая сумма", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.totalDeposited)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text("Депозиты", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.income)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                                Text("Доходность", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(Dimens.RadiusPill))
                                    .background(colors.surfaceVariant)
                            ) {
                                val depositRatio = if (uiState.finalAmount > 0) (uiState.totalDeposited / uiState.finalAmount).toFloat() else 0f
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRoundRect(
                                        color = colors.primary,
                                        size = Size(size.width * depositRatio, size.height),
                                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                    )
                                    drawRect(
                                        color = colors.secondary,
                                        topLeft = Offset(size.width * depositRatio, 0f),
                                        size = Size(size.width * (1f - depositRatio), size.height)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}