package ru.plumsoftware.finance.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Account
import ru.plumsoftware.finance.domain.model.AccountType
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat

data class AccountEditorUiState(
    val accountId: Long = 0L,
    val name: String = "",
    val type: AccountType = AccountType.DEBIT,
    val currencyCode: String = "RUB",
    val emoji: String = "💳",
    val initialBalanceDigits: String = "",
    val isDefault: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
)

class AccountEditorViewModel(
    private val editAccountId: Long?,
    private val accountRepository: AccountRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountEditorUiState())
    val uiState: StateFlow<AccountEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val defaultCurrency = settingsRepository.settings.first().defaultCurrencyCode
            if (editAccountId != null && editAccountId > 0L) {
                val account = accountRepository.getById(editAccountId)
                if (account != null) {
                    _uiState.value = AccountEditorUiState(
                        accountId = account.id,
                        name = account.name,
                        type = account.type,
                        currencyCode = account.currencyCode,
                        emoji = account.emoji,
                        initialBalanceDigits = MoneyFormat.minorToMajorDigits(
                            account.initialBalanceMinor,
                            account.currencyCode,
                        ),
                        isDefault = account.isDefault,
                    )
                }
            } else {
                _uiState.update { it.copy(currencyCode = defaultCurrency) }
            }
        }
    }

    fun setName(value: String) = _uiState.update { it.copy(name = value.take(40)) }
    fun setType(type: AccountType) = _uiState.update { it.copy(type = type) }
    fun setCurrency(code: String) = _uiState.update { it.copy(currencyCode = code) }
    fun setEmoji(value: String) = _uiState.update { it.copy(emoji = value.take(2)) }
    fun setInitialBalanceDigits(value: String) = _uiState.update { it.copy(initialBalanceDigits = value) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val initialMinor = MoneyFormat.majorDigitsToMinor(
                state.initialBalanceDigits.ifBlank { "0" },
                state.currencyCode,
            )
            accountRepository.upsert(
                Account(
                    id = state.accountId,
                    name = state.name.trim(),
                    type = state.type,
                    currencyCode = state.currencyCode,
                    emoji = state.emoji.ifBlank { "💳" },
                    initialBalanceMinor = initialMinor,
                    isDefault = state.isDefault,
                    sortOrder = 0,
                    createdAtMillis = System.currentTimeMillis(),
                ),
            )
            _uiState.update { it.copy(isSaving = false, saved = true) }
            onSaved()
        }
    }
}
