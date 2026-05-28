package ru.plumsoftware.finance.presentation.addtransaction

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
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountMajorDigits: String = "",
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val currencyCode: String = "RUB",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val currency = settings.defaultCurrencyCode
            val categories = categoryRepository.observeByType(CategoryType.EXPENSE, false).first()
            _uiState.update {
                it.copy(
                    currencyCode = currency,
                    categories = categories,
                    selectedCategoryId = categories.firstOrNull()?.id,
                )
            }
        }
    }

    fun setType(type: TransactionType) {
        if (type == TransactionType.SAVINGS) return
        _uiState.update { it.copy(type = type, errorMessage = null) }
        viewModelScope.launch {
            val categoryType = when (type) {
                TransactionType.INCOME -> CategoryType.INCOME
                TransactionType.EXPENSE -> CategoryType.EXPENSE
                TransactionType.SAVINGS -> CategoryType.EXPENSE
            }
            val categories = categoryRepository.observeByType(categoryType, false).first()
            _uiState.update {
                it.copy(
                    categories = categories,
                    selectedCategoryId = categories.firstOrNull()?.id,
                )
            }
        }
    }

    fun appendDigit(digit: String) {
        _uiState.update {
            val current = it.amountMajorDigits
            val next = when (digit) {
                "." -> {
                    when {
                        current.isEmpty() -> "0."
                        current.contains(".") -> current
                        else -> "$current."
                    }
                }
                else -> {
                    if (!digit.first().isDigit()) current
                    else if (current.contains(".")) {
                        val fractional = current.substringAfter(".", "")
                        if (fractional.length >= 2) current else current + digit
                    } else {
                        val normalized = (current + digit).trimStart('0')
                        if (normalized.isEmpty()) "0" else normalized.take(9)
                    }
                }
            }
            it.copy(amountMajorDigits = next, errorMessage = null)
        }
    }

    fun backspace() {
        _uiState.update {
            it.copy(
                amountMajorDigits = it.amountMajorDigits.dropLast(1).let { value ->
                    if (value == "0") "" else value
                },
                errorMessage = null,
            )
        }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun selectCategory(id: Long) {
        _uiState.update { it.copy(selectedCategoryId = id) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun save() {
        val state = _uiState.value
        val amount = MoneyFormat.majorDigitsToMinor(state.amountMajorDigits, state.currencyCode)
        when {
            amount <= 0L -> {
                _uiState.update { it.copy(errorMessage = "Укажите сумму") }
            }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                runCatching {
                    transactionRepository.upsert(
                        Transaction(
                            type = state.type,
                            amountMinor = amount,
                            categoryId = state.selectedCategoryId,
                            smartAssetId = null,
                            note = state.note.ifBlank { null },
                            dateMillis = System.currentTimeMillis(),
                            createdAtMillis = System.currentTimeMillis(),
                        ),
                    )
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = e.message ?: "Ошибка сохранения",
                        )
                    }
                }
            }
        }
    }
}
