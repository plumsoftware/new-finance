package ru.plumsoftware.finance.presentation.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.data.notifications.LimitAlertService
import ru.plumsoftware.finance.domain.budget.BudgetMath
import ru.plumsoftware.finance.domain.budget.BudgetService
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.MonthPeriod
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import java.time.LocalDate

enum class BudgetSource { EXPLICIT, LIMITS, AVERAGE, NONE }

data class LimitItem(
    val category: Category,
    val spentMinor: Long,
    val limitMinor: Long?,
) {
    val status: BudgetMath.LimitStatus get() = BudgetMath.limitStatus(spentMinor, limitMinor ?: 0)
    val ratio: Float get() = if ((limitMinor ?: 0) > 0) spentMinor.toFloat() / limitMinor!! else 0f
    val suggestedMinor: Long get() = BudgetMath.suggestLimit(spentMinor)
}

data class LimitsUiState(
    val withLimit: List<LimitItem> = emptyList(),
    val withoutLimit: List<LimitItem> = emptyList(),
    val currencyCode: String = "RUB",
    val budgetMinor: Long = 0,
    val budgetSource: BudgetSource = BudgetSource.NONE,
    val explicitBudgetMinor: Long? = null,
) {
    val okCount: Int get() = withLimit.count { it.status == BudgetMath.LimitStatus.OK }
    val almostCount: Int get() = withLimit.count { it.status == BudgetMath.LimitStatus.ALMOST }
    val exceededCount: Int get() = withLimit.count { it.status == BudgetMath.LimitStatus.EXCEEDED }
}

/** Лимиты (§6.11, §8.4) и общий бюджет месяца (§8.1). */
class LimitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    transactionRepository: TransactionRepository,
    private val limitAlerts: LimitAlertService,
) : ViewModel() {

    val uiState: StateFlow<LimitsUiState> = combine(
        categoryRepository.getCategoryWithSpending(MonthPeriod.current()),
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = false),
        settingsRepository.settings,
        transactionRepository.observeAll(),
    ) { spending, cats, settings, tx ->
        val spentById = spending.associate { it.category.id to it.spentMinor }
        val items = cats.map { LimitItem(it, spentById[it.id] ?: 0L, it.monthlyLimitMinor?.takeIf { l -> l > 0 }) }
        val limitsSum = items.sumOf { it.limitMinor ?: 0L }
        val budget = BudgetService.compute(tx, cats, settings.monthlyBudgetMinor, LocalDate.now(), DateFmt::toLocalDate)?.budget?.budget ?: 0L
        LimitsUiState(
            withLimit = items.filter { it.limitMinor != null }.sortedByDescending { it.ratio },
            withoutLimit = items.filter { it.limitMinor == null }.sortedByDescending { it.spentMinor },
            currencyCode = settings.defaultCurrencyCode,
            budgetMinor = budget,
            budgetSource = when {
                settings.monthlyBudgetMinor != null -> BudgetSource.EXPLICIT
                limitsSum > 0 -> BudgetSource.LIMITS
                budget > 0 -> BudgetSource.AVERAGE
                else -> BudgetSource.NONE
            },
            explicitBudgetMinor = settings.monthlyBudgetMinor,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LimitsUiState())

    fun setLimit(categoryId: Long, limitMinor: Long?) {
        viewModelScope.launch {
            categoryRepository.setLimit(categoryId, limitMinor?.takeIf { it > 0 })
            if (limitMinor != null) limitAlerts.check(categoryId)
        }
    }

    fun setBudget(minor: Long?) {
        viewModelScope.launch { settingsRepository.update { it.copy(monthlyBudgetMinor = minor?.takeIf { v -> v > 0 }) } }
    }
}
