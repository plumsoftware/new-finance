package ru.plumsoftware.finance.presentation.addtransaction

import android.content.Context
import android.net.Uri
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
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.R

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountMajorDigits: String = "",
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Long = 1L,
    val currencyCode: String = "RUB",
    val showCurrencyPicker: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
    val quickCategorySaving: Boolean = false,
)

class AddTransactionViewModel(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
) : ViewModel() {
    private val quickCategoryName: String? = savedStateHandle.get<String>("quickCategory")
        ?.let(Uri::decode)
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val accounts = accountRepository.observeAllActive().first()
            val selectedAccount = accounts.find { it.id == settings.selectedAccountId }
                ?: accounts.firstOrNull()
            val currency = selectedAccount?.currencyCode ?: settings.defaultCurrencyCode
            val categories = categoryRepository.observeByType(CategoryType.EXPENSE, false).first()
            _uiState.update {
                it.copy(
                    accounts = accounts,
                    selectedAccountId = selectedAccount?.id ?: 1L,
                    currencyCode = currency,
                    categories = categories,
                    selectedCategoryId = resolveInitialCategoryId(categories),
                )
            }
        }
    }

    fun selectAccount(accountId: Long) {
        val account = _uiState.value.accounts.find { it.id == accountId } ?: return
        _uiState.update {
            it.copy(
                selectedAccountId = accountId,
                currencyCode = account.currencyCode,
            )
        }
        viewModelScope.launch {
            settingsRepository.update { settings -> settings.copy(selectedAccountId = accountId) }
        }
    }

    fun setCurrency(code: String) {
        _uiState.update { it.copy(currencyCode = code, showCurrencyPicker = false) }
    }

    fun openCurrencyPicker() {
        _uiState.update { it.copy(showCurrencyPicker = true) }
    }

    fun closeCurrencyPicker() {
        _uiState.update { it.copy(showCurrencyPicker = false) }
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
                    selectedCategoryId = resolveInitialCategoryId(categories),
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

    private fun resolveInitialCategoryId(categories: List<Category>): Long? {
        val quickName = quickCategoryName ?: return categories.firstOrNull()?.id
        return categories.firstOrNull { category ->
            category.name.equals(quickName, ignoreCase = true)
        }?.id ?: categories.firstOrNull()?.id
    }

    fun save() {
        val state = _uiState.value
        val amount = MoneyFormat.majorDigitsToMinor(state.amountMajorDigits, state.currencyCode)
        when {
            amount <= 0L -> {
                _uiState.update { it.copy(errorMessage = context.getString(R.string.error_enter_amount)) }
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
                            accountId = state.selectedAccountId,
                            currencyCode = state.currencyCode,
                            originalAmountMinor = amount,
                            originalCurrencyCode = state.currencyCode,
                            exchangeRate = 1.0,
                        ),
                    )
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, saved = true) }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = e.message ?: context.getString(R.string.error_save_failed),
                        )
                    }
                }
            }
        }
    }

    fun createQuickCategory(
        name: String,
        icon: String,
        colorArgb: Long,
        onCreated: () -> Unit = {},
    ) {
        if (name.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = context.getString(R.string.error_enter_category_name))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(quickCategorySaving = true, errorMessage = null) }
            val categoryType = when (_uiState.value.type) {
                TransactionType.INCOME -> CategoryType.INCOME
                else -> CategoryType.EXPENSE
            }
            val trimmedName = name.trim().take(30)
            runCatching {
                if (categoryRepository.existsByNameIgnoreCase(categoryType, trimmedName)) {
                    error(context.getString(R.string.category_duplicate_name))
                }
                val existing = categoryRepository.observeByType(categoryType, includeHidden = true).first()
                val newId = categoryRepository.upsert(
                    Category(
                        name = trimmedName,
                        type = categoryType,
                        icon = icon,
                        colorArgb = colorArgb,
                        isHidden = false,
                        isSystem = false,
                        sortOrder = (existing.minOfOrNull { it.sortOrder } ?: 0) - 1,
                    )
                )
                val updatedCategories = categoryRepository.observeByType(categoryType, false).first()
                _uiState.update {
                    it.copy(
                        categories = updatedCategories,
                        selectedCategoryId = newId,
                    )
                }
            }.onSuccess {
                _uiState.update { it.copy(quickCategorySaving = false) }
                onCreated()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        quickCategorySaving = false,
                        errorMessage = e.message ?: context.getString(R.string.error_create_category_failed),
                    )
                }
            }
        }
    }
}
