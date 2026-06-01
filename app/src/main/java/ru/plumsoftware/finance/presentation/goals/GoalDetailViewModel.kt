package ru.plumsoftware.finance.presentation.goals

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.GoalDeposit
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.model.remainingMinor
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.R

data class GoalDetailUiState(
    val goal: Goal? = null,
    val deposits: List<GoalDeposit> = emptyList(),
    val currencyCode: String = "RUB",
    val showDepositSheet: Boolean = false,
    val amountDigits: String = "",
    val depositNote: String = "",
    val isSaving: Boolean = false,
    val showCelebration: Boolean = false,
)

class GoalDetailViewModel(
    private val goalId: Long,
    private val goalRepository: GoalRepository,
    private val transactionRepository: TransactionRepository,
    settingsRepository: SettingsRepository,
    private val context: Context,
) : ViewModel() {
    private val mutableState = MutableStateFlow(GoalDetailUiState())
    val transientState = mutableState.asStateFlow()

    val uiState: StateFlow<GoalDetailUiState> = combine(
        goalRepository.observeGoal(goalId),
        goalRepository.observeDeposits(goalId),
        settingsRepository.settings,
        mutableState,
    ) { goal, deposits, settings, transient ->
        transient.copy(
            goal = goal,
            deposits = deposits,
            currencyCode = settings.defaultCurrencyCode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalDetailUiState())

    fun openDepositSheet() {
        val goal = uiState.value.goal ?: return
        val defaultDigits = MoneyFormat.minorToMajorDigits(goal.remainingMinor, uiState.value.currencyCode)
        mutableState.update {
            it.copy(
                showDepositSheet = true,
                amountDigits = if (defaultDigits == "0") "" else defaultDigits,
                depositNote = "",
            )
        }
    }

    fun closeDepositSheet() {
        mutableState.update { it.copy(showDepositSheet = false) }
    }

    fun appendDepositDigit(digit: String) {
        mutableState.update { state ->
            val normalized = normalizeDigits(state.amountDigits + digit)
            state.copy(amountDigits = normalized)
        }
    }

    fun backspaceDeposit() {
        mutableState.update { it.copy(amountDigits = it.amountDigits.dropLast(1)) }
    }

    fun setDepositAmount(amountMinor: Long) {
        val digits = MoneyFormat.minorToMajorDigits(amountMinor, uiState.value.currencyCode)
        mutableState.update { it.copy(amountDigits = digits) }
    }

    fun setDepositNote(note: String) {
        mutableState.update { it.copy(depositNote = note.take(120)) }
    }

    fun confirmDeposit() {
        val state = uiState.value
        val goal = state.goal ?: return
        val amountMinor = MoneyFormat.majorDigitsToMinor(state.amountDigits, state.currencyCode)
            .coerceAtMost(goal.remainingMinor)
        if (amountMinor <= 0L) return
        viewModelScope.launch {
            mutableState.update { it.copy(isSaving = true) }
            val wasCompleted = goal.isCompleted
            runCatching {
                val updatedGoal = goalRepository.addDeposit(
                    goalId = goalId,
                    amountMinor = amountMinor,
                    note = state.depositNote,
                )
                val now = System.currentTimeMillis()
                transactionRepository.upsert(
                    Transaction(
                        type = TransactionType.INCOME,
                        amountMinor = amountMinor,
                        categoryId = null,
                        smartAssetId = null,
                        note = buildString {
                            append(
                                context.getString(
                                    R.string.goal_deposit_transaction_note_prefix,
                                    updatedGoal.name,
                                ),
                            )
                            state.depositNote.takeIf { it.isNotBlank() }?.let {
                                append(context.getString(R.string.common_dash_separator))
                                append(it)
                            }
                        },
                        dateMillis = now,
                        createdAtMillis = now,
                    ),
                )
                updatedGoal
            }.onSuccess { updated ->
                mutableState.update {
                    it.copy(
                        showDepositSheet = false,
                        isSaving = false,
                        showCelebration = !wasCompleted && updated.isCompleted,
                        amountDigits = "",
                        depositNote = "",
                    )
                }
            }.onFailure {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissCelebration() {
        mutableState.update { it.copy(showCelebration = false) }
    }

    fun deleteGoal(onDeleted: () -> Unit) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            onDeleted()
        }
    }

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
