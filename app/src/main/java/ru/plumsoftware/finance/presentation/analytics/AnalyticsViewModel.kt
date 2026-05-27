package ru.plumsoftware.finance.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.domain.model.PeriodSummary
import ru.plumsoftware.finance.domain.model.SavingsIndex
import ru.plumsoftware.finance.domain.repository.AnalyticsRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.StatsPeriod
import ru.plumsoftware.finance.presentation.common.resolveRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnalyticsUiState(
    val period: StatsPeriod = StatsPeriod.MONTH,
    val summary: PeriodSummary = PeriodSummary(0, 0, 0, 0),
    val topCategories: List<CategorySpending> = emptyList(),
    val dailyBars: List<Pair<String, Long>> = emptyList(),
    val savingsIndex: SavingsIndex = SavingsIndex(0, 0),
    val currencyCode: String = "RUB",
    val isLoading: Boolean = true,
    val showDateRangePicker: Boolean = false,
    val customStartMillis: Long? = null,
    val customEndMillis: Long? = null,
    val periodLabel: String? = null,
)

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load(StatsPeriod.MONTH)
    }

    fun selectPeriod(period: StatsPeriod) {
        if (period == StatsPeriod.CUSTOM) {
            _uiState.update { it.copy(showDateRangePicker = true, period = StatsPeriod.CUSTOM) }
        } else {
            load(period)
        }
    }

    fun dismissDateRangePicker() {
        _uiState.update { it.copy(showDateRangePicker = false) }
    }

    fun applyCustomRange(startMillis: Long, endMillis: Long) {
        _uiState.update {
            it.copy(
                customStartMillis = startMillis,
                customEndMillis = endMillis,
                showDateRangePicker = false,
            )
        }
        load(StatsPeriod.CUSTOM)
    }

    private fun load(period: StatsPeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, period = period, showDateRangePicker = false) }
            val state = _uiState.value
            val currency = settingsRepository.settings.first().defaultCurrencyCode
            val range = period.resolveRange(
                customStartMillis = state.customStartMillis,
                customEndMillis = state.customEndMillis,
            )
            val summary = analyticsRepository.getPeriodSummary(range.startMillis, range.endMillis)
            val top = categoryRepository.getTopSpendingByCategory(range.startMillis, range.endMillis, 8)
            val savings = analyticsRepository.getSavingsIndex(range.startMillis, range.endMillis)
            val daily = transactionRepository.observeDailySummaries(range.startMillis, range.endMillis).first()
            val formatter = SimpleDateFormat("dd.MM", Locale("ru"))
            val rangeFormatter = SimpleDateFormat("d MMM yyyy", Locale("ru"))
            val periodLabel = if (period == StatsPeriod.CUSTOM) {
                "${rangeFormatter.format(Date(range.startMillis))} — ${rangeFormatter.format(Date(range.endMillis))}"
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    summary = summary,
                    topCategories = top,
                    dailyBars = daily.map { day -> formatter.format(Date(day.dateMillis)) to day.expenseMinor },
                    savingsIndex = savings,
                    currencyCode = currency,
                    isLoading = false,
                    periodLabel = periodLabel,
                )
            }
        }
    }
}
