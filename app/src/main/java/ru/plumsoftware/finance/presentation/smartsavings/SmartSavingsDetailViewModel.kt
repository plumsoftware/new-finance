package ru.plumsoftware.finance.presentation.smartsavings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetUsage
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat

data class SmartSavingsDetailUiState(
    val asset: SmartAsset? = null,
    val usages: List<SmartAssetUsage> = emptyList(),
    val currencyCode: String = "RUB",
    val showRecordSheet: Boolean = false,
    val amountDigits: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class SmartSavingsDetailViewModel(
    private val assetId: Long,
    private val smartAssetRepository: SmartAssetRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartSavingsDetailUiState())
    val uiState: StateFlow<SmartSavingsDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val currency = settingsRepository.settings.first().defaultCurrencyCode
            val data = smartAssetRepository.getWithUsages(assetId)
            _uiState.update {
                it.copy(
                    asset = data?.asset,
                    usages = data?.usages.orEmpty().sortedByDescending { u -> u.usedAtMillis },
                    currencyCode = currency,
                    amountDigits = data?.asset?.let { a ->
                        MoneyFormat.minorToMajorDigits(a.alternativeCostMinor, currency)
                    }.orEmpty(),
                )
            }
        }
    }

    fun openRecordSheet() {
        val asset = _uiState.value.asset ?: return
        val defaultDigits = MoneyFormat.minorToMajorDigits(asset.alternativeCostMinor, _uiState.value.currencyCode)
        _uiState.update {
            it.copy(showRecordSheet = true, amountDigits = defaultDigits, errorMessage = null)
        }
    }

    fun closeRecordSheet() {
        _uiState.update { it.copy(showRecordSheet = false, errorMessage = null) }
    }

    fun appendDigit(d: String) {
        _uiState.update {
            val combined = (it.amountDigits + d).filter { c -> c.isDigit() }
            val normalized = when {
                combined.isEmpty() -> ""
                else -> {
                    val t = combined.trimStart('0')
                    if (t.isEmpty()) "0" else t.take(9)
                }
            }
            it.copy(amountDigits = normalized)
        }
    }

    fun backspace() = _uiState.update { it.copy(amountDigits = it.amountDigits.dropLast(1)) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun recordSaving() {
        val state = _uiState.value
        val amount = MoneyFormat.majorDigitsToMinor(state.amountDigits, state.currencyCode)
        if (amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Укажите сумму экономии") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                smartAssetRepository.recordUsage(
                    smartAssetId = assetId,
                    savedAmountMinor = amount,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, showRecordSheet = false) }
                load()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Ошибка")
                }
            }
        }
    }

    // --- МЕТОД УДАЛЕНИЯ ---
    fun deleteAsset(onDeleted: () -> Unit) {
        viewModelScope.launch {
            smartAssetRepository.deleteAssetWithUsages(assetId)
            onDeleted()
        }
    }
}
