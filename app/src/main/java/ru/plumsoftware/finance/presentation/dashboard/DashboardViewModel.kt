package ru.plumsoftware.finance.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository

data class DashboardUiState(
    val totalBalanceMinor: Long = 0L,
    val currencyCode: String = "RUB",
    val monthIncomeMinor: Long = 0L,
    val monthExpenseMinor: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryMap: Map<Long, Category> = emptyMap(),
    val smartAssets: List<SmartAsset> = emptyList(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null,
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val smartAssetRepository: SmartAssetRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _snackbar = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        transactionRepository.observeAll(),
        smartAssetRepository.observeActive(),
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
        categoryRepository.observeByType(CategoryType.INCOME, includeHidden = true),
        settingsRepository.settings,
        _snackbar,
    ) { values ->
        val transactions = values[0] as List<Transaction>
        val assets = values[1] as List<SmartAsset>
        val expenseCategories = values[2] as List<Category>
        val incomeCategories = values[3] as List<Category>
        val settings = values[4] as ru.plumsoftware.finance.domain.model.AppSettings
        val snackbar = values[5] as String?
        val monthRange = currentMonthRange()
        val monthTx = transactions.filter { it.dateMillis in monthRange.first..monthRange.second }
        val totalBalance = transactions.sumOf { tx ->
            when (tx.type) {
                TransactionType.INCOME -> tx.amountMinor
                TransactionType.EXPENSE -> -tx.amountMinor
                TransactionType.SAVINGS -> 0L
            }
        }
        DashboardUiState(
            totalBalanceMinor = totalBalance,
            currencyCode = settings.defaultCurrencyCode,
            monthIncomeMinor = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
            monthExpenseMinor = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor },
            recentTransactions = transactions.take(5),
            categoryMap = (expenseCategories + incomeCategories).associateBy { it.id },
            smartAssets = assets,
            isLoading = false,
            snackbarMessage = snackbar,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun recordSmartUsage(assetId: Long) {
        viewModelScope.launch {
            runCatching { smartAssetRepository.recordUsage(smartAssetId = assetId) }
                .onSuccess {
                    _snackbar.value = "Сэкономлено! +1 использование"
                }
                .onFailure {
                    _snackbar.value = it.message ?: "Не удалось сохранить"
                }
        }
    }

    fun clearSnackbar() {
        _snackbar.value = null
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.delete(id)
            _snackbar.value = "Операция удалена"
        }
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        val end = start.clone() as Calendar
        end.add(Calendar.MONTH, 1)
        end.add(Calendar.MILLISECOND, -1)
        return start.timeInMillis to end.timeInMillis
    }
}
