package ru.plumsoftware.finance.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.RecurringRepository
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import kotlinx.coroutines.flow.combine

data class RecurringUiState(
    val items: List<RecurringTransaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categoryMap: Map<Long, Category> = emptyMap(),
)

class RecurringViewModel(
    private val recurringRepository: RecurringRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    val uiState: StateFlow<RecurringUiState> = combine(
        recurringRepository.observeAll(),
        categoryRepository.observeByType(CategoryType.EXPENSE, false),
        categoryRepository.observeByType(CategoryType.INCOME, false),
    ) { items, expense, income ->
        val categories = expense + income
        RecurringUiState(
            items = items,
            categories = categories,
            categoryMap = categories.associateBy { it.id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringUiState())

    fun toggleActive(id: Long, active: Boolean) {
        viewModelScope.launch { recurringRepository.toggleActive(id, active) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { recurringRepository.delete(id) }
    }

    fun add(transaction: RecurringTransaction) {
        viewModelScope.launch { recurringRepository.add(transaction) }
    }
}
