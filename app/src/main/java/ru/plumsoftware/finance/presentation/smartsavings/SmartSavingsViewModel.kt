package ru.plumsoftware.finance.presentation.smartsavings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository

data class SmartSavingsUiState(
    val totalSavedMinor: Long = 0L,
    val payingOff: List<SmartAsset> = emptyList(),
    val profit: List<SmartAsset> = emptyList(),
    val currencyCode: String = "RUB",
)

class SmartSavingsViewModel(
    private val smartAssetRepository: SmartAssetRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SmartSavingsUiState> = combine(
        smartAssetRepository.observeTotalSavedAllTime(),
        smartAssetRepository.observeByStatus(SmartAssetStatus.PAYING_OFF),
        smartAssetRepository.observeByStatus(SmartAssetStatus.PROFIT),
        settingsRepository.settings,
    ) { total, paying, profit, settings ->
        SmartSavingsUiState(
            totalSavedMinor = total,
            payingOff = paying,
            profit = profit,
            currencyCode = settings.defaultCurrencyCode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SmartSavingsUiState())

    fun recordQuickUsage(assetId: Long) {
        viewModelScope.launch {
            smartAssetRepository.recordUsage(smartAssetId = assetId)
        }
    }
}
