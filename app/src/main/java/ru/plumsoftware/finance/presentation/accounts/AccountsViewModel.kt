package ru.plumsoftware.finance.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat

data class AccountsUiState(
    val accounts: List<AccountWithBalance> = emptyList(),
    val selectedAccountId: Long = 1L,
    val totalBalanceLabel: String = "",
)

class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = kotlinx.coroutines.flow.combine(
        accountRepository.observeAllWithBalances(),
        settingsRepository.settings,
    ) { accounts, settings ->
        val totalMinor = accounts.sumOf { it.calculatedBalanceMinor }
        val currency = accounts.firstOrNull()?.account?.currencyCode ?: settings.defaultCurrencyCode
        AccountsUiState(
            accounts = accounts,
            selectedAccountId = settings.selectedAccountId,
            totalBalanceLabel = MoneyFormat.format(totalMinor, currency),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())

    fun selectAccount(accountId: Long) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(selectedAccountId = accountId) }
        }
    }

    fun archiveAccount(accountId: Long) {
        viewModelScope.launch {
            accountRepository.archive(accountId)
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            accountRepository.delete(accountId)
            settingsRepository.update { settings ->
                if (settings.selectedAccountId == accountId) {
                    settings.copy(selectedAccountId = 1L)
                } else {
                    settings
                }
            }
        }
    }
}
