package ru.plumsoftware.finance.presentation.smartsavings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetTrackingMode
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat

val SMART_EMOJI_PRESETS = listOf("🛍️", "☕", "🎒", "🧴", "🚰", "🔌", "🧺", "🍱")

data class CreateSmartSavingsUiState(
    val currencyCode: String = "RUB",
    val name: String = "",
    val icon: String = "🛍️",
    val purchaseDigits: String = "",
    val savingPerUseDigits: String = "",
    val note: String = "",
    val recordPurchaseExpense: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val errorMessage: String? = null,
)

class CreateSmartSavingsViewModel(
    private val editAssetId: Long? = null,
    private val smartAssetRepository: SmartAssetRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSmartSavingsUiState())
    val uiState: StateFlow<CreateSmartSavingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currency = settingsRepository.settings.first().defaultCurrencyCode
            _uiState.update { it.copy(currencyCode = currency) }

            // Если передали ID, значит это режим РЕДАКТИРОВАНИЯ - загружаем данные
            if (editAssetId != null && editAssetId != 0L) {
                smartAssetRepository.getById(editAssetId)?.let { asset ->
                    _uiState.update {
                        it.copy(
                            name = asset.name,
                            icon = asset.icon,
                            note = asset.note ?: "",
                            purchaseDigits = MoneyFormat.minorToMajorDigits(asset.purchaseCostMinor, currency),
                            savingPerUseDigits = MoneyFormat.minorToMajorDigits(asset.alternativeCostMinor, currency)
                        )
                    }
                }
            }
        }
    }

    fun setName(v: String) = _uiState.update { it.copy(name = v, errorMessage = null) }
    fun setIcon(v: String) = _uiState.update { it.copy(icon = v) }
    fun setNote(v: String) = _uiState.update { it.copy(note = v) }
    fun setRecordPurchaseExpense(v: Boolean) = _uiState.update { it.copy(recordPurchaseExpense = v) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun appendPurchaseDigit(d: String) = _uiState.update { it.copy(purchaseDigits = normalizeDigits(it.purchaseDigits + d)) }
    fun backspacePurchase() = _uiState.update { it.copy(purchaseDigits = it.purchaseDigits.dropLast(1)) }
    fun appendSavingDigit(d: String) = _uiState.update { it.copy(savingPerUseDigits = normalizeDigits(it.savingPerUseDigits + d)) }
    fun backspaceSaving() = _uiState.update { it.copy(savingPerUseDigits = it.savingPerUseDigits.dropLast(1)) }

    fun save() {
        if (_uiState.value.isSaving) return
        val state = _uiState.value
        val currency = state.currencyCode
        val purchase = MoneyFormat.majorDigitsToMinor(state.purchaseDigits, currency)
        val savingDigits = state.savingPerUseDigits.ifBlank { state.purchaseDigits }
        val saving = MoneyFormat.majorDigitsToMinor(savingDigits, currency)

        when {
            state.name.isBlank() -> _uiState.update { it.copy(errorMessage = "Укажите название") }
            purchase <= 0 -> _uiState.update { it.copy(errorMessage = "Укажите стоимость покупки") }
            saving <= 0 -> _uiState.update { it.copy(errorMessage = "Укажите экономию за раз") }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, errorMessage = null, saved = false) }

                try {
                    withContext(Dispatchers.IO) {
                        if (editAssetId != null && editAssetId != 0L) {
                            val existing = smartAssetRepository.getById(editAssetId)
                            if (existing != null) {
                                val updated = existing.copy(
                                    name = state.name.trim(),
                                    icon = state.icon,
                                    purchaseCostMinor = purchase,
                                    alternativeCostMinor = saving,
                                    note = state.note.ifBlank { null }
                                )
                                smartAssetRepository.update(updated)
                            }
                        } else {
                            smartAssetRepository.create(
                                asset = SmartAsset(
                                    name = state.name.trim(),
                                    icon = state.icon,
                                    purchaseCostMinor = purchase,
                                    alternativeCostMinor = saving,
                                    trackingMode = SmartAssetTrackingMode.MANUAL,
                                    status = SmartAssetStatus.PAYING_OFF,
                                    totalSavedMinor = 0,
                                    totalUses = 0,
                                    purchasedAtMillis = System.currentTimeMillis(),
                                    isActive = true,
                                    note = state.note.ifBlank { null },
                                    createdAtMillis = System.currentTimeMillis(),
                                ),
                                categoryId = null,
                                createPurchaseExpense = state.recordPurchaseExpense,
                            )
                        }
                    }
                    _uiState.update { it.copy(saved = true, isSaving = false) }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        _uiState.update { it.copy(errorMessage = e.message ?: "Ошибка", isSaving = false) }
                    }
                }
            }
        }
    }

    private fun normalizeDigits(raw: String): String {
        val combined = raw.filter { it.isDigit() }
        if (combined.isEmpty()) return ""
        val t = combined.trimStart('0')
        return if (t.isEmpty()) "0" else t.take(9)
    }
}
