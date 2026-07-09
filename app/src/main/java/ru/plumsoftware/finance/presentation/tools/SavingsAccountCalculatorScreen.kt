package ru.plumsoftware.finance.presentation.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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


data class SavingsMonthRow(
    val month: Int,
    val startBalance: Double,
    val interestEarned: Double,
    val endBalance: Double
)

data class SavingsAccountUiState(
    val initialAmount: String = "",
    val rate: String = "8.0",
    val monthlyTopUp: String = "",
    val monthlyWithdrawal: String = "",
    val termMonths: String = "12",
    val isDailyBalance: Boolean = true, // true = ежедневный остаток, false = минимальный остаток
    val isReinvested: Boolean = true,  // true = реинвестировать (капитализировать) проценты
    val finalBalance: Double = 0.0,
    val totalInterestAccrued: Double = 0.0,
    val schedule: List<SavingsMonthRow> = emptyList(),
    val hasResult: Boolean = false
) {
    val canCalculate: Boolean
        get() = initialAmount.cleanDouble() != null &&
                rate.cleanDouble() != null &&
                termMonths.cleanInt() != null
}

class SavingsAccountCalculatorViewModel : ViewModel() {
    var state by mutableStateOf(SavingsAccountUiState())
        private set

    fun onInitialAmountChanged(value: String) { state = state.copy(initialAmount = value) }
    fun onRateChanged(value: String) { state = state.copy(rate = value) }
    fun onTopUpChanged(value: String) { state = state.copy(monthlyTopUp = value) }
    fun onWithdrawalChanged(value: String) { state = state.copy(monthlyWithdrawal = value) }
    fun onTermChanged(value: String) { state = state.copy(termMonths = value) }
    fun setCalculationMode(isDaily: Boolean) { state = state.copy(isDailyBalance = isDaily) }
    fun toggleReinvestment(enabled: Boolean) { state = state.copy(isReinvested = enabled) }

    fun calculate() {
        val initial = state.initialAmount.cleanDouble() ?: return
        val annualRate = state.rate.cleanDouble() ?: return
        val months = state.termMonths.cleanInt() ?: return
        val topUp = state.monthlyTopUp.cleanDouble() ?: 0.0
        val withdrawal = state.monthlyWithdrawal.cleanDouble() ?: 0.0
        val isReinvested = state.isReinvested

        var currentBalance = initial
        var totalInterest = 0.0
        val monthlyRate = annualRate / 100.0 / 12.0

        val calculatedSchedule = (1..months).map { m ->
            val startBal = currentBalance

            // Финансовая модель:
            // 1. Пополнение счета происходит в 1-й день месяца
            val balanceAfterTopUp = currentBalance + topUp

            // 2. Снятие со счета происходит в середине месяца (на 15-й день)
            val finalBalanceBeforeInterest = (balanceAfterTopUp - withdrawal).coerceAtLeast(0.0)

            // 3. Расчет процентов по выбранной базе начисления
            val interestEarned = if (state.isDailyBalance) {
                // На ежедневный остаток:
                // 15 дней месяца на счету лежала сумма (Баланс + Пополнение)
                // 15 дней месяца на счету лежала сумма (Баланс + Пополнение - Снятие)
                val sumDailyBalances = (balanceAfterTopUp * 15) + (finalBalanceBeforeInterest * 15)
                val avgDailyBalance = sumDailyBalances / 30.0
                avgDailyBalance * monthlyRate
            } else {
                // На минимальный остаток:
                // Берется минимальное значение, зафиксированное на счету в течение любого из дней месяца
                val minBalance = minOf(startBal, balanceAfterTopUp, finalBalanceBeforeInterest)
                minBalance * monthlyRate
            }

            // 4. Начисление процентов и капитализация
            // Если реинвестирование включено, проценты добавляются к телу вклада.
            // Если выключено — выплачиваются отдельно (текущий баланс не увеличивается на процент).
            currentBalance = if (isReinvested) {
                finalBalanceBeforeInterest + interestEarned
            } else {
                finalBalanceBeforeInterest
            }
            totalInterest += interestEarned

            SavingsMonthRow(
                month = m,
                startBalance = startBal,
                interestEarned = interestEarned,
                // Для отображения в графике выводим реальное количество денег на счету в конце месяца
                endBalance = if (isReinvested) currentBalance else finalBalanceBeforeInterest + interestEarned
            )
        }

        state = state.copy(
            finalBalance = currentBalance,
            totalInterestAccrued = totalInterest,
            schedule = calculatedSchedule,
            hasResult = true
        )
    }
}

@Composable
fun SavingsAccountCalculatorScreen(
    onBack: () -> Unit,
    vm: SavingsAccountCalculatorViewModel = viewModel()
) {
    val uiState = vm.state
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = { ToolHeader(title = "Накопительный счет", onBack = onBack) },
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
                            label = "Начальный остаток",
                            value = uiState.initialAmount,
                            onValueChange = vm::onInitialAmountChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Процентная ставка",
                            value = uiState.rate,
                            onValueChange = vm::onRateChanged,
                            placeholder = "8.0",
                            suffix = "%"
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
                            label = "Снятие в месяц",
                            value = uiState.monthlyWithdrawal,
                            onValueChange = vm::onWithdrawalChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Срок расчета",
                            value = uiState.termMonths,
                            onValueChange = vm::onTermChanged,
                            placeholder = "12",
                            suffix = "мес.",
                            isIntegerOnly = true
                        )
                        HorizontalDivider()

                        // База начисления (выбор типа расчета)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.SpacingS),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)
                        ) {
                            Text(
                                text = "База начисления",
                                style = typography.bodyLarge,
                                color = colors.onSurface
                            )
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
                                        .background(if (uiState.isDailyBalance) colors.primary else Color.Transparent)
                                        .clickable { vm.setCalculationMode(true) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Ежедневный остаток",
                                        style = typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (uiState.isDailyBalance) Color.White else colors.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                                        .background(if (!uiState.isDailyBalance) colors.primary else Color.Transparent)
                                        .clickable { vm.setCalculationMode(false) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Минимальный остаток",
                                        style = typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (!uiState.isDailyBalance) Color.White else colors.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        // Переключатель реинвестирования (капитализации) процентов
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.SpacingS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Реинвестировать доход",
                                    style = typography.bodyLarge,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = "Прибавлять начисленные проценты к балансу",
                                    style = typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.isReinvested,
                                onCheckedChange = vm::toggleReinvestment,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.primary
                                )
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
                            Text("Результаты прогноза", style = typography.titleMedium, fontWeight = FontWeight.Bold)

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.finalBalance)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34C759)
                                )
                                Text("Итоговый баланс счета", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.totalInterestAccrued)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                                Text("Всего начислено процентов", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (uiState.hasResult && uiState.schedule.isNotEmpty()) {
                item {
                    Text(
                        "Помесячный график",
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
                                Text("+${formatOutput(row.interestEarned)} ₽", style = typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF34C759))
                                Text(
                                    text = "Баланс: ${formatOutput(row.endBalance)} ₽",
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