package ru.plumsoftware.finance.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.analytics.AnalyticsMath
import ru.plumsoftware.finance.domain.analytics.BucketUnit
import ru.plumsoftware.finance.domain.analytics.DateRange
import ru.plumsoftware.finance.domain.analytics.PeriodMode
import ru.plumsoftware.finance.domain.budget.BudgetService
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.ui.ds.BarKind
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.roundToLong

data class AnalyticsBar(val start: LocalDate, val end: LocalDate, val value: Long, val kind: BarKind, val axisLabel: String?)

data class CategoryStat(val category: Category, val amount: Long, val share: Float, val excluded: Boolean, val relative: Float)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val mode: PeriodMode = PeriodMode.MONTH,
    val range: DateRange = DateRange(LocalDate.now().withDayOfMonth(1), LocalDate.now()),
    val unit: BucketUnit = BucketUnit.DAY,
    val currencyCode: String = "RUB",
    val total: Long = 0,
    val comparePercent: Int? = null,
    val monthlyAverage: Long = 0,
    val bars: List<AnalyticsBar> = emptyList(),
    val categories: List<CategoryStat> = emptyList(),
    val excludedIds: Set<Long> = emptySet(),
    val forecast: Long = 0,
    val forecastBudget: Long? = null,
    val forecastIsFinal: Boolean = false,
    val income: Long = 0,
    val savedPercent: Int? = null,
    val minDate: LocalDate = LocalDate.now(),
) {
    val excludedNames: List<String> get() = categories.filter { it.excluded }.map { it.category.name }
}

/** Аналитика (§6.4, §8.3): все расчёты учитывают выбранный период и исключённые категории. */
class AnalyticsViewModel(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val mode = MutableStateFlow(PeriodMode.MONTH)
    private val custom = MutableStateFlow<DateRange?>(null)

    private val _events = MutableStateFlow<AnalyticsEvent?>(null)
    val events: StateFlow<AnalyticsEvent?> = _events

    val uiState: StateFlow<AnalyticsUiState> = combine(
        transactionRepository.observeAll(),
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
        settingsRepository.settings,
        mode,
        custom,
    ) { tx, cats, settings, m, c ->
        val today = LocalDate.now()
        val range = AnalyticsMath.range(m, today, c)
        val excluded = settings.analyticsExcludedCategoryIds
        fun included(id: Long?) = id == null || id !in excluded

        val expenses = tx.filter { it.type == TransactionType.EXPENSE }.map { it to DateFmt.toLocalDate(it.dateMillis) }
        val inRange = expenses.filter { it.second in range }
        val inc = inRange.filter { included(it.first.categoryId) }
        val total = inc.sumOf { it.first.amountMinor }

        val prev = AnalyticsMath.previousRange(m, range, today)
        val prevTotal = prev?.let { r -> expenses.filter { it.second in r && included(it.first.categoryId) }.sumOf { it.first.amountMinor } } ?: 0L
        val compare = AnalyticsMath.comparePercent(total, prevTotal)

        // Столбцы.
        val byDate = inc.groupBy { it.second }.mapValues { e -> e.value.sumOf { it.first.amountMinor } }
        val unit = AnalyticsMath.bucketUnit(m, range)
        val buckets = AnalyticsMath.buckets(range, unit, byDate)
        val labels = AnalyticsMath.axisLabelIndices(m, unit, buckets)
        val past = buckets.filter { !it.start.isAfter(today) && it.total > 0 }
        val avg = if (past.isEmpty()) 0.0 else past.sumOf { it.total }.toDouble() / past.size
        val bars = buckets.mapIndexed { i, b ->
            val kind = when {
                b.start.isAfter(today) || b.total <= 0 -> BarKind.EMPTY
                today in DateRange(b.start, b.end) -> BarKind.CURRENT
                b.total > avg * 1.25 -> BarKind.HIGH
                else -> BarKind.DEFAULT
            }
            val label = if (i !in labels) null else when {
                m == PeriodMode.WEEK -> DateFmt.weekdayShort(b.start)
                unit == BucketUnit.MONTH -> DateFmt.monthStandaloneShort(b.start)
                else -> b.start.dayOfMonth.toString()
            }
            AnalyticsBar(b.start, b.end, b.total, kind, label)
        }

        // Категории: доля от суммы включённых; ширина — относительно самой крупной.
        val byCat = inRange.groupBy { it.first.categoryId }.mapValues { e -> e.value.sumOf { it.first.amountMinor } }
        val maxCat = byCat.filterKeys { included(it) }.values.maxOrNull()?.takeIf { it > 0 } ?: 1L
        val stats = cats.filter { (byCat[it.id] ?: 0L) > 0 || it.id in excluded }.map { cat ->
            val amount = byCat[cat.id] ?: 0L
            val isExcluded = cat.id in excluded
            CategoryStat(
                category = cat,
                amount = amount,
                share = if (!isExcluded && total > 0) amount.toFloat() / total else 0f,
                excluded = isExcluded,
                relative = if (isExcluded) amount.toFloat() / maxCat else amount.toFloat() / maxCat,
            )
        }.sortedWith(compareBy<CategoryStat> { it.excluded }.thenByDescending { it.amount })

        // Прогноз на конец периода с учётом исключений.
        val elapsed = AnalyticsMath.elapsedDays(range, today)
        val isFinal = elapsed >= range.lengthDays
        val forecast = if (elapsed <= 0) 0L else if (isFinal) total
        else ((total.toDouble() / elapsed * range.lengthDays) / 10_000.0).roundToLong() * 10_000L
        val monthBudget = BudgetService.compute(tx, cats, settings.monthlyBudgetMinor, today, DateFmt::toLocalDate)?.budget?.budget
        val forecastBudget = monthBudget?.let {
            if (m == PeriodMode.MONTH) it else (it.toDouble() / YearMonth.from(today).lengthOfMonth() * range.lengthDays).roundToLong()
        }
        val income = tx.filter { it.type == TransactionType.INCOME && DateFmt.toLocalDate(it.dateMillis) in range }.sumOf { it.amountMinor }
        val saved = if (income > 0) (((income - total).toDouble() / income) * 100).toInt() else null
        val months = (ChronoUnit.MONTHS.between(YearMonth.from(range.start), YearMonth.from(minOf(today, range.end))) + 1).coerceAtLeast(1)

        AnalyticsUiState(
            isLoading = false,
            mode = m,
            range = range,
            unit = unit,
            currencyCode = settings.defaultCurrencyCode,
            total = total,
            comparePercent = compare,
            monthlyAverage = total / months,
            bars = bars,
            categories = stats,
            excludedIds = excluded,
            forecast = forecast,
            forecastBudget = forecastBudget,
            forecastIsFinal = isFinal,
            income = income,
            savedPercent = saved,
            minDate = tx.minOfOrNull { it.dateMillis }?.let(DateFmt::toLocalDate) ?: today,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    fun setMode(m: PeriodMode) {
        mode.value = m
    }

    fun setCustomRange(range: DateRange) {
        custom.value = range
        mode.value = PeriodMode.CUSTOM
    }

    /** Включение/исключение категории; нельзя исключить последнюю (§6.4 п.5). */
    fun toggleCategory(id: Long) {
        viewModelScope.launch {
            val state = uiState.value
            val excluded = settingsRepository.settings.first().analyticsExcludedCategoryIds
            if (id !in excluded) {
                val remaining = state.categories.count { !it.excluded && it.category.id != id }
                if (remaining == 0) {
                    _events.value = AnalyticsEvent.LastCategory
                    return@launch
                }
            }
            settingsRepository.update {
                it.copy(analyticsExcludedCategoryIds = if (id in excluded) excluded - id else excluded + id)
            }
        }
    }

    fun restoreAll() {
        viewModelScope.launch { settingsRepository.update { it.copy(analyticsExcludedCategoryIds = emptySet()) } }
    }

    fun consumeEvent() {
        _events.value = null
    }
}

sealed interface AnalyticsEvent {
    data object LastCategory : AnalyticsEvent
}
