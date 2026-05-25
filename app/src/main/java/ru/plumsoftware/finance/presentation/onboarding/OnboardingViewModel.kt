package ru.plumsoftware.finance.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.repository.SettingsRepository

data class OnboardingUiState(
    val currentPage: Int = 0,
    val pageCount: Int = 4,
    val isCompleting: Boolean = false,
)

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun completeOnboarding() {
        if (_uiState.value.isCompleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }
            settingsRepository.update { settings ->
                settings.copy(onboardingCompleted = true)
            }
            _uiState.update { it.copy(isCompleting = false) }
        }
    }
}
