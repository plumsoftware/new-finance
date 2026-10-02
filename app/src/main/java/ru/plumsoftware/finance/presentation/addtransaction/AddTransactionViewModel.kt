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
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.notifications.LimitAlertService
import ru.plumsoftware.finance.domain.budget.BudgetService
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.receipt.FnsReceipt
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.ui.ds.AmountInput
import java.time.LocalDate
import java.time.ZoneId

/** Шаблон быстрого ввода из недавних операций (§6.2 п.4). */
data class RecentTemplate(
    val emoji: String,
    val title: String,
    val amountMinor: Long,
    val categoryId: Long?,
    val note: String?,
)

data class AddTransactionUiState(
    val isEdit: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val input: String = "",
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val accounts: List<Account> = emptyList(),
    val selectedAccountId: Long = 1L,
    val currencyCode: String = "RUB",
    val templates: List<RecentTemplate> = emptyList(),
    val expandRecent: Boolean = false,
    val dateMillis: Long? = null,
    val isScanning: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val amountMinor: Long get() = AmountInput.toMinor(input)
    val canSave: Boolean get() = amountMinor > 0 && !isSaving
}

/** Результат сохранения — текст снекбара Коппи (§6.2). */
data class SaveResult(val message: String, val overLimit: Boolean)

class AddTransactionViewModel(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val limitAlerts: LimitAlertService,
    private val context: Context,
) : ViewModel() {

    private val quickCategoryName: String? = savedStateHandle.get<String>("quickCategory")
        ?.let(Uri::decode)?.trim()?.takeIf { it.isNotBlank() }
    private val editId: Long? = savedStateHandle.get<String>("editId")?.toLongOrNull()?.takeIf { it > 0 }
    val startWithScan: Boolean = savedStateHandle.get<String>("scan") == "true"
    private var editing: Transaction? = null

    private val _uiState = MutableStateFlow(
        AddTransactionUiState(expandRecent = savedStateHandle.get<String>("recent") == "true", isEdit = editId != null),
    )
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _saved = MutableStateFlow<SaveResult?>(null)
    val saved: StateFlow<SaveResult?> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val accounts = accountRepository.observeAllActive().first()
            val existing = editId?.let { transactionRepository.getById(it) }
            editing = existing
            val type = existing?.type?.takeIf { it != TransactionType.SAVINGS } ?: TransactionType.EXPENSE
            val selectedAccount = accounts.find { it.id == (existing?.accountId ?: settings.selectedAccountId) }
                ?: accounts.firstOrNull()
            val categories = loadCategories(type)
            _uiState.update {
                it.copy(
                    type = type,
                    accounts = accounts,
                    selectedAccountId = selectedAccount?.id ?: 1L,
                    currencyCode = existing?.currencyCode ?: selectedAccount?.currencyCode ?: settings.defaultCurrencyCode,
                    categories = categories,
                    selectedCategoryId = existing?.categoryId ?: resolveInitialCategoryId(categories),
                    input = existing?.let { e -> AmountInput.fromMinor(e.amountMinor) } ?: "",
                    note = existing?.note.orEmpty(),
                    dateMillis = existing?.dateMillis,
                    templates = loadTemplates(type, categories),
                )
            }
        }
    }

    private suspend fun loadCategories(type: TransactionType): List<Category> =
        categoryRepository.observeByType(
            if (type == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE,
            false,
        ).first()

    /** 4–6 недавних уникальных операций выбранного типа. */
    private suspend fun loadTemplates(type: TransactionType, categories: List<Category>): List<RecentTemplate> {
        val catMap = categories.associateBy { it.id }
        return transactionRepository.observeAll().first()
            .filter { it.type == type }
            .sortedByDescending { it.dateMillis }
            .take(80)
            .distinctBy { Triple(it.categoryId, it.note?.trim()?.lowercase(), it.amountMinor) }
            .take(6)
            .map { tx ->
                val cat = tx.categoryId?.let { catMap[it] }
                RecentTemplate(
                    emoji = cat?.icon ?: "💸",
                    title = tx.note?.takeIf { it.isNotBlank() } ?: cat?.name.orEmpty(),
                    amountMinor = tx.amountMinor,
                    categoryId = tx.categoryId,
                    note = tx.note,
                )
            }
    }

    private fun resolveInitialCategoryId(categories: List<Category>): Long? {
        val quickName = quickCategoryName ?: return categories.firstOrNull()?.id
        return categories.firstOrNull { it.name.equals(quickName, ignoreCase = true) }?.id ?: categories.firstOrNull()?.id
    }

    fun setType(type: TransactionType) {
        if (type == TransactionType.SAVINGS || type == _uiState.value.type) return
        viewModelScope.launch {
            val categories = loadCategories(type)
            _uiState.update {
                it.copy(
                    type = type,
                    categories = categories,
                    selectedCategoryId = categories.firstOrNull()?.id,
                    templates = loadTemplates(type, categories),
                    errorMessage = null,
                )
            }
        }
    }

    fun onKey(key: String) = _uiState.update { it.copy(input = AmountInput.press(it.input, key), errorMessage = null) }

    fun setNote(note: String) = _uiState.update { it.copy(note = note.take(120)) }

    fun selectCategory(id: Long) = _uiState.update { it.copy(selectedCategoryId = id) }

    fun selectAccount(accountId: Long) {
        val account = _uiState.value.accounts.find { it.id == accountId } ?: return
        _uiState.update { it.copy(selectedAccountId = accountId, currencyCode = account.currencyCode) }
        viewModelScope.launch { settingsRepository.update { it.copy(selectedAccountId = accountId) } }
    }

    /** Тап по шаблону заполняет сумму, категорию и заметку. */
    fun applyTemplate(t: RecentTemplate) = _uiState.update {
        it.copy(
            input = AmountInput.fromMinor(t.amountMinor),
            selectedCategoryId = t.categoryId ?: it.selectedCategoryId,
            note = t.note.orEmpty(),
            errorMessage = null,
        )
    }

    fun setScanning(scanning: Boolean) = _uiState.update { it.copy(isScanning = scanning) }

    /** Результат сканера: `null` — пользователь закрыл камеру. */
    fun onScanResult(raw: String?) {
        if (raw == null) {
            setScanning(false)
            return
        }
        val receipt = FnsReceipt.parse(raw)
        if (receipt == null) {
            _uiState.update { it.copy(isScanning = false, errorMessage = context.getString(R.string.add_scan_not_found)) }
            return
        }
        viewModelScope.launch {
            val millis = receipt.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val noteText = context.getString(R.string.add_scan_note, DateFmt.dayMonthShort(receipt.dateTime.toLocalDate()), DateFmt.time(millis))
            if (_uiState.value.type != TransactionType.EXPENSE) setTypeSync(TransactionType.EXPENSE)
            _uiState.update {
                it.copy(
                    isScanning = false,
                    input = AmountInput.fromMinor(receipt.amountMinor),
                    dateMillis = millis,
                    note = noteText,
                    errorMessage = null,
                )
            }
            settingsRepository.update { it.copy(receiptsScanned = it.receiptsScanned + 1) }
        }
    }

    private suspend fun setTypeSync(type: TransactionType) {
        val categories = loadCategories(type)
        _uiState.update {
            it.copy(type = type, categories = categories, selectedCategoryId = categories.firstOrNull()?.id, templates = loadTemplates(type, categories))
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun save() {
        val state = _uiState.value
        val amount = state.amountMinor
        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.error_enter_amount)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val now = System.currentTimeMillis()
            runCatching {
                val base = editing
                transactionRepository.upsert(
                    Transaction(
                        id = base?.id ?: 0,
                        type = state.type,
                        amountMinor = amount,
                        categoryId = state.selectedCategoryId,
                        smartAssetId = base?.smartAssetId,
                        goalId = base?.goalId,
                        note = state.note.trim().ifBlank { null },
                        dateMillis = state.dateMillis ?: now,
                        createdAtMillis = base?.createdAtMillis ?: now,
                        accountId = state.selectedAccountId,
                        currencyCode = state.currencyCode,
                        originalAmountMinor = amount,
                        originalCurrencyCode = state.currencyCode,
                        exchangeRate = 1.0,
                    ),
                )
                if (state.type == TransactionType.EXPENSE) limitAlerts.check(state.selectedCategoryId)
            }.onSuccess {
                _saved.value = buildSaveResult(state)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: context.getString(R.string.error_save_failed))
                }
            }
        }
    }

    /** «Записал. Сегодня можно ещё X ₽» или «Лимит дня превышен на X ₽». */
    private suspend fun buildSaveResult(state: AddTransactionUiState): SaveResult {
        val settings = settingsRepository.settings.first()
        val cats = categoryRepository.observeByType(CategoryType.EXPENSE, true).first()
        val tx = transactionRepository.observeAll().first()
        val budget = BudgetService.compute(tx, cats, settings.monthlyBudgetMinor, LocalDate.now(), DateFmt::toLocalDate)?.budget
        val currency = settings.defaultCurrencyCode
        return when {
            budget == null -> SaveResult(context.getString(R.string.add_saved_plain), false)
            budget.left >= 0 -> SaveResult(context.getString(R.string.add_saved_left, Money.formatRounded(budget.left, currency)), false)
            else -> SaveResult(context.getString(R.string.add_saved_over, Money.formatRounded(-budget.left, currency)), true)
        }
    }
}
