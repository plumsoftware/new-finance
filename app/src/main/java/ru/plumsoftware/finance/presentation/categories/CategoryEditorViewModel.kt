package ru.plumsoftware.finance.presentation.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.repository.CategoryRepository

private val expenseEmojis = listOf(
    "🛒", "☕", "🚌", "💊", "🎬", "👗", "🏠", "💡", "📱", "🍕",
    "💈", "🐾", "🎮", "📚", "✈️", "🏋️", "💄", "🔧", "🎁", "🍺",
)
private val incomeEmojis = listOf(
    "💼", "💻", "📈", "🎓", "🏦", "💰", "🎯", "🏆", "🤝", "🎪",
    "💡", "🛠️", "🎨", "📝", "🔑", "⭐", "🚀", "🌟", "💎", "🎤",
)

val categoryColors = listOf(
    0xFFFF3B30, 0xFFFF9500, 0xFFFFCC00, 0xFF34C759, 0xFF00C7BE,
    0xFF30B0C7, 0xFF007AFF, 0xFF5856D6, 0xFFAF52DE, 0xFFFF2D55,
)

data class CategoryEditorUiState(
    val categoryId: Long? = null,
    val isEdit: Boolean = false,
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val icon: String = "🛒",
    val colorArgb: Long = 0xFFFF3B30,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = name.trim().isNotEmpty()
    val availableEmojis: List<String> get() = if (type == CategoryType.EXPENSE) expenseEmojis else incomeEmojis
}

class CategoryEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val categoryIdArg = savedStateHandle.get<String>("categoryId")?.toLongOrNull()
    private val typeArg = savedStateHandle.get<String>("type")

    private val _uiState = MutableStateFlow(
        CategoryEditorUiState(
            categoryId = categoryIdArg,
            isEdit = categoryIdArg != null,
            type = if (typeArg == "INCOME") CategoryType.INCOME else CategoryType.EXPENSE,
            icon = if (typeArg == "INCOME") "💼" else "🛒",
            colorArgb = if (typeArg == "INCOME") 0xFF34C759 else 0xFFFF3B30,
        )
    )
    val uiState: StateFlow<CategoryEditorUiState> = _uiState.asStateFlow()

    init {
        if (categoryIdArg != null) {
            viewModelScope.launch {
                categoryRepository.getById(categoryIdArg)?.let { category ->
                    _uiState.update {
                        it.copy(
                            isEdit = true,
                            categoryId = category.id,
                            name = category.name,
                            type = category.type,
                            icon = category.icon,
                            colorArgb = category.colorArgb ?: if (category.type == CategoryType.INCOME) 0xFF34C759 else 0xFFFF3B30,
                        )
                    }
                }
            }
        }
    }

    fun setName(value: String) {
        _uiState.update { it.copy(name = value.take(30), error = null) }
    }

    fun setType(type: CategoryType) {
        _uiState.update {
            it.copy(
                type = type,
                icon = if (type == CategoryType.INCOME) "💼" else "🛒",
                colorArgb = if (type == CategoryType.INCOME) 0xFF34C759 else 0xFFFF3B30,
            )
        }
    }

    fun setIcon(icon: String) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun setColor(colorArgb: Long) {
        _uiState.update { it.copy(colorArgb = colorArgb) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching {
                val currentList = categoryRepository.observeByType(state.type, includeHidden = true).first()
                val nextSortOrder = if (state.isEdit) {
                    currentList.firstOrNull { it.id == state.categoryId }?.sortOrder ?: currentList.size
                } else {
                    (currentList.maxOfOrNull { it.sortOrder } ?: -1) + 1
                }
                categoryRepository.upsert(
                    Category(
                        id = state.categoryId ?: 0L,
                        name = state.name.trim(),
                        type = state.type,
                        icon = state.icon,
                        colorArgb = state.colorArgb,
                        isHidden = false,
                        isSystem = false,
                        sortOrder = nextSortOrder,
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Ошибка сохранения") }
            }
        }
    }
}

