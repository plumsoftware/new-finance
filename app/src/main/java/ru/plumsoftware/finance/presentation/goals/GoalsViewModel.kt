package ru.plumsoftware.finance.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository

data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val activeGoals: List<Goal> = emptyList(),
    val completedGoals: List<Goal> = emptyList(),
    val totalSavedMinor: Long = 0L,
    val overallProgress: Float = 0f,
    val currencyCode: String = "RUB",
)

class GoalsViewModel(
    goalRepository: GoalRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<GoalsUiState> = combine(
        goalRepository.observeGoals(),
        settingsRepository.settings,
    ) { goals, settings ->
        val activeGoals = goals.filterNot { it.isCompleted }
        val completedGoals = goals.filter { it.isCompleted }
        val totalSaved = goals.sumOf { it.savedAmountMinor }
        val totalTarget = goals.sumOf { it.targetAmountMinor }.coerceAtLeast(1L)
        GoalsUiState(
            goals = goals,
            activeGoals = activeGoals,
            completedGoals = completedGoals,
            totalSavedMinor = totalSaved,
            overallProgress = (totalSaved.toFloat() / totalTarget.toFloat()).coerceIn(0f, 1f),
            currencyCode = settings.defaultCurrencyCode,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        GoalsUiState(),
    )
}
