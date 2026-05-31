package ru.plumsoftware.finance.presentation.smartsavings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository

data class SmartSavingsUiState(
    val totalSavedMinor: Long = 0L,
    val payingOff: List<SmartAsset> = emptyList(),
    val profit: List<SmartAsset> = emptyList(),
    val showCompletedOnly: Boolean = false,
    val currencyCode: String = "RUB",
) {
    val displayedAssets: List<SmartAsset>
        get() = if (showCompletedOnly) profit else payingOff
}

class SmartSavingsViewModel(
    private val smartAssetRepository: SmartAssetRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val showCompletedOnly = MutableStateFlow(false)

    val uiState: StateFlow<SmartSavingsUiState> = combine(
        smartAssetRepository.observeTotalSavedAllTime(),
        smartAssetRepository.observeByStatus(SmartAssetStatus.PAYING_OFF),
        smartAssetRepository.observeByStatus(SmartAssetStatus.PROFIT),
        settingsRepository.settings,
        showCompletedOnly,
    ) { total, paying, profit, settings, completedFilter ->
        SmartSavingsUiState(
            totalSavedMinor = total,
            payingOff = paying,
            profit = profit,
            showCompletedOnly = completedFilter,
            currencyCode = settings.defaultCurrencyCode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SmartSavingsUiState())

    fun toggleCompletedFilter() {
        showCompletedOnly.update { !it }
    }

    fun recordQuickUsage(assetId: Long) {
        viewModelScope.launch {
            smartAssetRepository.recordUsage(smartAssetId = assetId)
        }
    }
}
