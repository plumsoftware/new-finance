package ru.plumsoftware.finance.presentation.categories

import android.content.Context
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
import ru.plumsoftware.finance.ui.theme.CategoryUiDefaults
import ru.plumsoftware.finance.R

private val expenseEmojis = listOf(
    "🛒", "☕", "🚌", "🏠", "💊", "🎬",
    "👗", "💡", "📱", "🍕", "🐾", "🎮",
    "📚", "✈️", "🏋️", "💄", "🔧", "🎁",
)
private val incomeEmojis = listOf(
    "💼", "💻", "📈", "🏦", "💰", "🎓",
    "🎯", "🏆", "🤝", "🚀", "🎨", "📝",
    "🔑", "⭐", "💎", "🎤", "🛠️", "🌟",
)

val categoryColors = listOf(
    CategoryUiDefaults.DEFAULT_COLOR_ARGB,
    0xFFFF3B30, 0xFFFF9500, 0xFFFFCC00, 0xFF34C759, 0xFF00C7BE,
    0xFF30B0C7, 0xFF007AFF, 0xFF5856D6, 0xFFAF52DE, 0xFFFF2D55,
    0xFFA2845E, 0xFF5AC8FA, 0xFF64D2FF, 0xFFBF5AF2,
    0xFFDAA520, 0xFF228B22, 0xFF4B0082, 0xFF2E8B57, 0xFFB22222,
)

data class CategoryEditorUiState(
    val categoryId: Long? = null,
    val isEdit: Boolean = false,
    val name: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val icon: String = "🛒",
    val colorArgb: Long = CategoryUiDefaults.DEFAULT_COLOR_ARGB,
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
    private val context: Context,
) : ViewModel() {

    private val categoryIdArg = savedStateHandle.get<String>("categoryId")?.toLongOrNull()
    private val typeArg = savedStateHandle.get<String>("type")

    private val _uiState = MutableStateFlow(
        CategoryEditorUiState(
            categoryId = categoryIdArg,
            isEdit = categoryIdArg != null,
            type = if (typeArg == "INCOME") CategoryType.INCOME else CategoryType.EXPENSE,
            icon = if (typeArg == "INCOME") "💼" else "🛒",
            colorArgb = CategoryUiDefaults.DEFAULT_COLOR_ARGB,
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
                            colorArgb = category.colorArgb ?: CategoryUiDefaults.DEFAULT_COLOR_ARGB,
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
                colorArgb = CategoryUiDefaults.DEFAULT_COLOR_ARGB,
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
                val trimmedName = state.name.trim()
                if (categoryRepository.existsByNameIgnoreCase(
                        state.type,
                        trimmedName,
                        excludeId = state.categoryId ?: 0L,
                    )
                ) {
                    error(context.getString(R.string.category_duplicate_name))
                }
                val currentList = categoryRepository.observeByType(state.type, includeHidden = true).first()
                val nextSortOrder = if (state.isEdit) {
                    currentList.firstOrNull { it.id == state.categoryId }?.sortOrder ?: currentList.size
                } else {
                    (currentList.minOfOrNull { it.sortOrder } ?: 0) - 1
                }
                categoryRepository.upsert(
                    Category(
                        id = state.categoryId ?: 0L,
                        name = trimmedName,
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

