package ru.plumsoftware.finance.presentation.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val goalExpenseEmojis = listOf(
    "🎯", "📱", "🚗", "🏠", "💍", "🎮",
    "✈️", "🎓", "🛋️", "📷", "💻", "⌚",
    "🧳", "🏖️", "🧸", "🎁", "📚", "🎸",
)

data class CreateGoalUiState(
    val goalId: Long? = null,
    val isEdit: Boolean = false,
    val currencyCode: String = "RUB",
    val name: String = "",
    val note: String = "",
    val emoji: String = "🎯",
    val colorHex: String = "#007AFF",
    val targetDigits: String = "",
    val savedDigits: String = "",
    val hasDeadline: Boolean = false,
    val deadlineMillis: Long? = null,
    val showOnHome: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && targetDigits.isNotBlank()
    val deadlineFormatted: String
        get() = deadlineMillis?.let {
            SimpleDateFormat("d MMM yyyy", Locale("ru")).format(Date(it))
        } ?: "—"
}

class CreateGoalViewModel(
    savedStateHandle: SavedStateHandle,
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val editGoalId = savedStateHandle.get<String>("goalId")?.toLongOrNull()

    private val _uiState = MutableStateFlow(CreateGoalUiState(goalId = editGoalId, isEdit = editGoalId != null))
    val uiState: StateFlow<CreateGoalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currency = settingsRepository.settings.first().defaultCurrencyCode
            _uiState.update { it.copy(currencyCode = currency) }
            if (editGoalId != null) {
                goalRepository.getGoal(editGoalId)?.let { goal ->
                    _uiState.update {
                        it.copy(
                            name = goal.name,
                            note = goal.note.orEmpty(),
                            emoji = goal.emoji,
                            colorHex = goal.colorHex,
                            targetDigits = MoneyFormat.minorToMajorDigits(goal.targetAmountMinor, currency),
                            savedDigits = MoneyFormat.minorToMajorDigits(goal.savedAmountMinor, currency),
                            hasDeadline = goal.deadline != null,
                            deadlineMillis = goal.deadline,
                            showOnHome = goal.showOnHome,
                            createdAtMillis = goal.createdAtMillis,
                        )
                    }
                }
            }
        }
    }

    fun setName(value: String) = _uiState.update { it.copy(name = value.take(40)) }
    fun setNote(value: String) = _uiState.update { it.copy(note = value.take(120)) }
    fun setEmoji(value: String) = _uiState.update { it.copy(emoji = value) }
    fun setColor(value: String) = _uiState.update { it.copy(colorHex = value) }

    fun setTargetDigits(value: String) = _uiState.update { it.copy(targetDigits = normalizeDigits(value)) }
    fun setSavedDigits(value: String) = _uiState.update { it.copy(savedDigits = normalizeDigits(value)) }
    fun setShowOnHome(value: Boolean) = _uiState.update { it.copy(showOnHome = value) }

    fun toggleDeadline(enabled: Boolean) = _uiState.update {
        it.copy(
            hasDeadline = enabled,
            deadlineMillis = if (enabled) it.deadlineMillis ?: System.currentTimeMillis() else null,
        )
    }

    fun setDeadline(millis: Long) = _uiState.update { it.copy(deadlineMillis = millis, hasDeadline = true) }

    fun save() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val targetMinor = MoneyFormat.majorDigitsToMinor(state.targetDigits, state.currencyCode)
            val savedMinor = MoneyFormat.majorDigitsToMinor(state.savedDigits.ifBlank { "0" }, state.currencyCode)
            val normalizedSaved = savedMinor.coerceAtMost(targetMinor).coerceAtLeast(0L)
            val goal = Goal(
                id = state.goalId ?: 0L,
                name = state.name.trim(),
                emoji = state.emoji,
                targetAmountMinor = targetMinor,
                savedAmountMinor = normalizedSaved,
                colorHex = state.colorHex,
                deadline = if (state.hasDeadline) state.deadlineMillis else null,
                note = state.note.ifBlank { null },
                showOnHome = state.showOnHome,
                isCompleted = normalizedSaved >= targetMinor && targetMinor > 0L,
                createdAtMillis = state.createdAtMillis,
            )
            runCatching { goalRepository.upsertGoal(goal) }
                .onSuccess { _uiState.update { it.copy(isSaving = false, saved = true) } }
                .onFailure { _uiState.update { it.copy(isSaving = false) } }
        }
    }

    fun availableEmojis(): List<String> = goalExpenseEmojis

    private fun normalizeDigits(raw: String): String {
        val filtered = raw.filter { it.isDigit() || it == '.' }
        if (filtered.isEmpty()) return ""
        if (filtered == ".") return "0."
        val dotIndex = filtered.indexOf('.')
        return if (dotIndex >= 0) {
            val intPart = filtered.substring(0, dotIndex).filter { c -> c.isDigit() }
            val fracPart = filtered.substring(dotIndex + 1).filter { c -> c.isDigit() }.take(2)
            val safeInt = intPart.ifEmpty { "0" }.trimStart('0').ifEmpty { "0" }.take(9)
            "$safeInt.$fracPart"
        } else {
            filtered.filter { c -> c.isDigit() }.trimStart('0').ifEmpty { "0" }.take(9)
        }
    }
}
