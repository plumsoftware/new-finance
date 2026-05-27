package ru.plumsoftware.finance.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository

data class DashboardUiState(
    val totalBalanceMinor: Long = 0L,
    val currencyCode: String = "RUB",
    val todayIncomeMinor: Long = 0L,
    val todayExpenseMinor: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList(),
    val smartAssets: List<SmartAsset> = emptyList(),
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null,
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val smartAssetRepository: SmartAssetRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _snackbar = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        transactionRepository.observeAll(),
        smartAssetRepository.observeActive(),
        settingsRepository.settings,
        _snackbar,
    ) { transactions, assets, settings, snackbar ->
        val todayStart = startOfDayMillis(System.currentTimeMillis())
        val todayEnd = endOfDayMillis(System.currentTimeMillis())
        val todayTx = transactions.filter { it.dateMillis in todayStart until todayEnd }
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
            todayIncomeMinor = todayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
            todayExpenseMinor = todayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor },
            recentTransactions = transactions.take(8),
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
}
