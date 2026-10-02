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

data class AccountEditorUiState(
    val accountId: Long = 0L,
    val name: String = "",
    val type: AccountType = AccountType.DEBIT,
    val currencyCode: String = "RUB",
    val emoji: String = "💳",
    val colorHex: String = "#007AFF",
    val initialBalanceMinor: Long = 0L,
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
                        colorHex = account.colorHex,
                        initialBalanceMinor = account.initialBalanceMinor,
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
    fun setColor(hex: String) = _uiState.update { it.copy(colorHex = hex) }
    fun setInitialBalance(minor: Long) = _uiState.update { it.copy(initialBalanceMinor = minor) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            // При редактировании сохраняем остальные поля счёта (порядок, дату создания, архив).
            val original = state.accountId.takeIf { it > 0 }?.let { accountRepository.getById(it) }
            val base = original ?: Account(
                name = "",
                type = state.type,
                currencyCode = state.currencyCode,
                createdAtMillis = System.currentTimeMillis(),
            )
            accountRepository.upsert(
                base.copy(
                    name = state.name.trim(),
                    type = state.type,
                    currencyCode = state.currencyCode,
                    emoji = state.emoji.ifBlank { "💳" },
                    colorHex = state.colorHex,
                    initialBalanceMinor = state.initialBalanceMinor,
                ),
            )
            _uiState.update { it.copy(isSaving = false, saved = true) }
            onSaved()
        }
    }
}
