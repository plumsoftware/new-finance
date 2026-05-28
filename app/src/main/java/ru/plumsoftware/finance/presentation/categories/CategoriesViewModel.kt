package ru.plumsoftware.finance.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.repository.CategoryRepository

data class CategoriesUiState(
    val selectedType: CategoryType = CategoryType.EXPENSE,
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
)

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val selectedType = MutableStateFlow(CategoryType.EXPENSE)

    val uiState: StateFlow<CategoriesUiState> = combine(
        selectedType,
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = false),
        categoryRepository.observeByType(CategoryType.INCOME, includeHidden = false),
    ) { type, expense, income ->
        CategoriesUiState(
            selectedType = type,
            expenseCategories = expense,
            incomeCategories = income,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriesUiState())

    fun selectType(type: CategoryType) {
        selectedType.update { type }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            categoryRepository.delete(id)
        }
    }
}

