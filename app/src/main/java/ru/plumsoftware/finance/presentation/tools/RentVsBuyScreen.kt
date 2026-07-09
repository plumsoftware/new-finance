package ru.plumsoftware.finance.presentation.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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

data class RentVsBuyUiState(
    val propPrice: String = "",
    val downPaymentPct: String = "",
    val mortgageRate: String = "",
    val termYears: String = "20",
    val rentCost: String = "",
    val propGrowth: String = "5.0",
    val investReturn: String = "10.0",
    val buyEquity: Double = 0.0,
    val rentPortfolio: Double = 0.0,
    val winner: String = "",
    val hasResult: Boolean = false
) {
    val canCalculate: Boolean
        get() = propPrice.cleanDouble() != null &&
                downPaymentPct.cleanDouble() != null &&
                mortgageRate.cleanDouble() != null &&
                termYears.cleanInt() != null &&
                rentCost.cleanDouble() != null
}

class RentVsBuyViewModel : ViewModel() {
    var state by mutableStateOf(RentVsBuyUiState())
        private set

    fun onPriceChanged(value: String) { state = state.copy(propPrice = value) }
    fun onDownPaymentChanged(value: String) { state = state.copy(downPaymentPct = value) }
    fun onRateChanged(value: String) { state = state.copy(mortgageRate = value) }
    fun onTermChanged(value: String) { state = state.copy(termYears = value) }
    fun onRentChanged(value: String) { state = state.copy(rentCost = value) }
    fun onGrowthChanged(value: String) { state = state.copy(propGrowth = value) }
    fun onReturnChanged(value: String) { state = state.copy(investReturn = value) }

    fun calculate() {
        val price = state.propPrice.cleanDouble() ?: return
        val downPct = state.downPaymentPct.cleanDouble() ?: return
        val rate = state.mortgageRate.cleanDouble() ?: return
        val years = state.termYears.cleanInt() ?: return
        val rent = state.rentCost.cleanDouble() ?: return
        val growth = state.propGrowth.cleanDouble() ?: 5.0
        val returnRate = state.investReturn.cleanDouble() ?: 10.0

        val downPayment = price * (downPct / 100.0)
        val loan = price - downPayment
        val r = rate / 100.0 / 12.0
        val months = years * 12

        val mortgagePayment = if (r == 0.0) loan / months else loan * r * (1 + r).pow(months) / ((1 + r).pow(months) - 1)
        val finalPropValue = price * (1.0 + growth / 100.0).pow(years)

        var portfolio = downPayment
        val monthlyInvestReturn = returnRate / 100.0 / 12.0

        repeat(months) {
            val diff = (mortgagePayment - rent).coerceAtLeast(0.0)
            portfolio = (portfolio + diff) * (1 + monthlyInvestReturn)
        }

        val buyEquity = finalPropValue
        val rentPortfolio = portfolio
        val isBuyWinner = buyEquity > rentPortfolio

        state = state.copy(
            buyEquity = buyEquity,
            rentPortfolio = rentPortfolio,
            winner = if (isBuyWinner) "Покупка выгоднее" else "Аренда и инвестиции выгоднее",
            hasResult = true
        )
    }
}

@Composable
fun RentVsBuyScreen(
    onBack: () -> Unit,
    vm: RentVsBuyViewModel = viewModel()
) {
    val uiState = vm.state
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        topBar = { ToolHeader(title = "Аренда или ипотека", onBack = onBack) },
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
                            label = "Цена недвижимости",
                            value = uiState.propPrice,
                            onValueChange = vm::onPriceChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Первоначальный взнос",
                            value = uiState.downPaymentPct,
                            onValueChange = vm::onDownPaymentChanged,
                            placeholder = "0.0",
                            suffix = "%"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Ставка ипотеки",
                            value = uiState.mortgageRate,
                            onValueChange = vm::onRateChanged,
                            placeholder = "12.0",
                            suffix = "%"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Срок кредита",
                            value = uiState.termYears,
                            onValueChange = vm::onTermChanged,
                            placeholder = "20",
                            suffix = "лет",
                            isIntegerOnly = true
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Стоимость аренды квартиры",
                            value = uiState.rentCost,
                            onValueChange = vm::onRentChanged,
                            placeholder = "0",
                            suffix = "₽"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Рост цены жилья в год",
                            value = uiState.propGrowth,
                            onValueChange = vm::onGrowthChanged,
                            placeholder = "5.0",
                            suffix = "%"
                        )
                        HorizontalDivider()
                        ToolInputField(
                            label = "Доходность инвестиций в год",
                            value = uiState.investReturn,
                            onValueChange = vm::onReturnChanged,
                            placeholder = "10.0",
                            suffix = "%"
                        )
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
                            Text("Выгода за срок расчета", style = typography.titleMedium, fontWeight = FontWeight.Bold)

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.buyEquity)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007AFF)
                                )
                                Text("Покупка (цена квартиры)", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Column {
                                Text(
                                    text = "${formatOutput(uiState.rentPortfolio)} ₽",
                                    style = typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34C759)
                                )
                                Text("Аренда (портфель инвестиций)", style = typography.bodySmall, color = colors.onSurfaceVariant)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Dimens.RadiusM))
                                    .background(colors.secondary.copy(alpha = 0.15f))
                                    .padding(Dimens.SpacingM),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.winner,
                                    style = typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}