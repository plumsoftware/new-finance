package ru.plumsoftware.finance.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import java.time.LocalDate

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val categoryMap: Map<Long, Category> = emptyMap(),
    val currencyCode: String = "RUB",
    val dateRangeStart: LocalDate? = null,
    val dateRangeEnd: LocalDate? = null,
) {
    val dateRangeActive: Boolean
        get() = dateRangeStart != null && dateRangeEnd != null
}

class HistoryViewModel(
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _dateRangeStart = MutableStateFlow<LocalDate?>(null)
    private val _dateRangeEnd = MutableStateFlow<LocalDate?>(null)

    init {
        pruneOldTransactions()
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        transactionRepository.observeAll(),
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
        categoryRepository.observeByType(CategoryType.INCOME, includeHidden = true),
        settingsRepository.settings,
        _dateRangeStart,
        _dateRangeEnd,
    ) { values ->
        val transactions = values[0] as List<Transaction>
        val expenseCategories = values[1] as List<Category>
        val incomeCategories = values[2] as List<Category>
        val settings = values[3] as ru.plumsoftware.finance.domain.model.AppSettings
        val dateRangeStart = values[4] as LocalDate?
        val dateRangeEnd = values[5] as LocalDate?
        val cutoffMillis = System.currentTimeMillis() - NINETY_DAYS_MILLIS
        val latestTransactions = transactions
            .asSequence()
            .filter { it.dateMillis >= cutoffMillis }
            .sortedByDescending { it.dateMillis }
            .take(90)
            .toList()
        HistoryUiState(
            transactions = latestTransactions,
            categoryMap = (expenseCategories + incomeCategories).associateBy { it.id },
            currencyCode = settings.defaultCurrencyCode,
            dateRangeStart = dateRangeStart,
            dateRangeEnd = dateRangeEnd,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getById(id)
            if (transaction?.type == TransactionType.SAVINGS && transaction.goalId != null) {
                goalRepository.deleteDepositByTransactionId(id)
            } else {
                transactionRepository.delete(id)
            }
        }
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _dateRangeStart.value = start
        _dateRangeEnd.value = end
    }

    fun clearDateRange() {
        _dateRangeStart.value = null
        _dateRangeEnd.value = null
    }

    private fun pruneOldTransactions() {
        viewModelScope.launch {
            val cutoffMillis = System.currentTimeMillis() - NINETY_DAYS_MILLIS
            val oldTransactionIds = transactionRepository.observeAll()
                .first()
                .asSequence()
                .filter { it.dateMillis < cutoffMillis }
                .map { it.id }
                .toList()
            oldTransactionIds.forEach { id ->
                val transaction = transactionRepository.getById(id)
                if (transaction?.type == TransactionType.SAVINGS && transaction.goalId != null) {
                    goalRepository.deleteDepositByTransactionId(id)
                } else {
                    transactionRepository.delete(id)
                }
            }
        }
    }

    private companion object {
        const val NINETY_DAYS_MILLIS = 90L * 24L * 60L * 60L * 1000L
    }
}
