package ru.plumsoftware.finance.presentation.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.CategoryWithSpending
import ru.plumsoftware.finance.domain.model.MonthPeriod
import ru.plumsoftware.finance.domain.notifications.LimitNotificationsEngine
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import kotlin.math.pow
import kotlin.math.roundToLong

data class LimitsUiState(
    val categoriesWithSpending: List<CategoryWithSpending> = emptyList(),
    val currencyCode: String = "RUB",
)

class LimitsViewModel(
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val limitNotificationsEngine: LimitNotificationsEngine,
) : ViewModel() {

    val categoriesWithSpending: StateFlow<List<CategoryWithSpending>> = combine(
        categoryRepository.getCategoryWithSpending(MonthPeriod.current()),
        settingsRepository.settings,
    ) { budgetItems, settings ->
        val currencyCode = settings.defaultCurrencyCode
        budgetItems.map { item ->
            CategoryWithSpending(
                category = item.category,
                spentThisMonth = item.spentMinor.toMajorAmount(currencyCode),
                limit = item.limitMinor?.toMajorAmount(currencyCode),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<LimitsUiState> = combine(
        categoriesWithSpending,
        settingsRepository.settings,
    ) { categories, settings ->
        LimitsUiState(
            categoriesWithSpending = categories,
            currencyCode = settings.defaultCurrencyCode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LimitsUiState())

    fun setLimit(categoryId: Long, limit: Double) {
        if (limit <= 0) return
        viewModelScope.launch {
            val currencyCode = uiState.value.currencyCode
            val limitMinor = limit.toMinorAmount(currencyCode)
            categoryRepository.setLimit(categoryId, limitMinor)
            val item = uiState.value.categoriesWithSpending.find { it.category.id == categoryId }
            if (item != null) {
                limitNotificationsEngine.evaluateAfterLimitSet(
                    category = item.category,
                    spentMinor = item.spentThisMonth.toMinorAmount(currencyCode),
                    limitMinor = limitMinor,
                    warningTitleRes = R.string.notif_limit_warning_title,
                    warningBodyRes = R.string.notif_limit_warning_body,
                    exceededTitleRes = R.string.notif_limit_exceeded_title,
                    exceededBodyRes = R.string.notif_limit_exceeded_body,
                    formatOverspend = { overspendMinor ->
                        formatMoneyDisplay(overspendMinor, currencyCode)
                    },
                )
            }
        }
    }

    fun removeLimit(categoryId: Long) {
        viewModelScope.launch {
            categoryRepository.setLimit(categoryId, null)
        }
    }
}

private fun Long.toMajorAmount(currencyCode: String): Double {
    val exp = java.util.Currency.getInstance(currencyCode).defaultFractionDigits.coerceAtLeast(0)
    return this / 10.0.pow(exp.toDouble())
}

private fun Double.toMinorAmount(currencyCode: String): Long {
    val exp = java.util.Currency.getInstance(currencyCode).defaultFractionDigits.coerceAtLeast(0)
    return (this * 10.0.pow(exp.toDouble())).roundToLong()
}

internal fun formatMoneyDisplay(amountMinor: Long, currencyCode: String): String =
    MoneyFormat.format(amountMinor, currencyCode)
        .replace(MoneyFormat.symbol(currencyCode), "")
        .trim()

internal fun Double.formatMoney(currencyCode: String): String =
    formatMoneyDisplay(toMinorAmount(currencyCode), currencyCode)
