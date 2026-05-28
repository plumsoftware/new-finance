package ru.plumsoftware.finance.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.CategorySpending
import ru.plumsoftware.finance.domain.model.PeriodSummary
import ru.plumsoftware.finance.domain.model.SavingsIndex
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.AnalyticsRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.StatsPeriod
import ru.plumsoftware.finance.presentation.common.resolveRange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnalyticsDailyBar(
    val label: String,
    val incomeMinor: Long,
    val expenseMinor: Long,
)

data class AnalyticsUiState(
    val period: StatsPeriod = StatsPeriod.MONTH,
    val summary: PeriodSummary = PeriodSummary(0, 0, 0, 0),
    val expenseCategories: List<CategorySpending> = emptyList(),
    val incomeCategories: List<CategorySpending> = emptyList(),
    val dailyBars: List<AnalyticsDailyBar> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val expenseCategoryMap: Map<Long, Category> = emptyMap(),
    val incomeCategoryMap: Map<Long, Category> = emptyMap(),
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

    fun refreshCurrentPeriod() {
        load(_uiState.value.period)
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

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.delete(id)
            refreshCurrentPeriod()
        }
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
            val savings = analyticsRepository.getSavingsIndex(range.startMillis, range.endMillis)
            val transactions = transactionRepository.observeByPeriod(range.startMillis, range.endMillis).first()
            val daily = transactionRepository.observeDailySummaries(range.startMillis, range.endMillis).first()
            val expenseCats = categoryRepository.observeByType(CategoryType.EXPENSE, true).first()
            val incomeCats = categoryRepository.observeByType(CategoryType.INCOME, true).first()
            val formatter = SimpleDateFormat("dd.MM", Locale("ru"))
            val rangeFormatter = SimpleDateFormat("d MMM yyyy", Locale("ru"))
            val periodLabel = if (period == StatsPeriod.CUSTOM) {
                "${rangeFormatter.format(Date(range.startMillis))} – ${rangeFormatter.format(Date(range.endMillis))}"
            } else {
                null
            }
            val expenseBreakdown = buildCategoryBreakdown(
                transactions = transactions,
                type = TransactionType.EXPENSE,
                categories = expenseCats,
            )
            val incomeBreakdown = buildCategoryBreakdown(
                transactions = transactions,
                type = TransactionType.INCOME,
                categories = incomeCats,
            )
            _uiState.update {
                it.copy(
                    summary = summary,
                    expenseCategories = expenseBreakdown,
                    incomeCategories = incomeBreakdown,
                    dailyBars = daily.map { day ->
                        AnalyticsDailyBar(
                            label = formatter.format(Date(day.dateMillis)),
                            incomeMinor = day.incomeMinor,
                            expenseMinor = day.expenseMinor,
                        )
                    },
                    transactions = transactions.sortedByDescending { tx -> tx.dateMillis },
                    expenseCategoryMap = expenseCats.associateBy { category -> category.id },
                    incomeCategoryMap = incomeCats.associateBy { category -> category.id },
                    savingsIndex = savings,
                    currencyCode = currency,
                    isLoading = false,
                    periodLabel = periodLabel,
                )
            }
        }
    }

    private fun buildCategoryBreakdown(
        transactions: List<Transaction>,
        type: TransactionType,
        categories: List<Category>,
    ): List<CategorySpending> {
        val byId = categories.associateBy { it.id }
        val totals = transactions
            .asSequence()
            .filter { it.type == type }
            .mapNotNull { tx ->
                val categoryId = tx.categoryId ?: return@mapNotNull null
                categoryId to tx.amountMinor
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, amounts) -> amounts.sum() }
        val totalAmount = totals.values.sum().coerceAtLeast(1L)
        return totals.entries
            .sortedByDescending { it.value }
            .mapNotNull { (categoryId, amount) ->
                val category = byId[categoryId] ?: return@mapNotNull null
                CategorySpending(
                    category = category,
                    amountMinor = amount,
                    sharePercent = amount.toFloat() / totalAmount * 100f,
                )
            }
    }
}
