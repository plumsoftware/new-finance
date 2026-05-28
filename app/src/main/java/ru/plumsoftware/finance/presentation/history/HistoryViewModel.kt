package ru.plumsoftware.finance.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val categoryMap: Map<Long, Category> = emptyMap(),
    val currencyCode: String = "RUB",
)

class HistoryViewModel(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        transactionRepository.observeAll(),
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
        categoryRepository.observeByType(CategoryType.INCOME, includeHidden = true),
        settingsRepository.settings,
    ) { transactions, expenseCategories, incomeCategories, settings ->
        HistoryUiState(
            transactions = transactions.sortedByDescending { it.dateMillis },
            categoryMap = (expenseCategories + incomeCategories).associateBy { it.id },
            currencyCode = settings.defaultCurrencyCode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.delete(id)
        }
    }
}
