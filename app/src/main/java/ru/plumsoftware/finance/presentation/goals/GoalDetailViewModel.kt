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
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.GoalDeposit
import ru.plumsoftware.finance.domain.model.remainingMinor
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/** Прогноз достижения цели при заданном ежемесячном темпе (§6.9 п.3). */
data class GoalPlan(
    val paceMinor: Long,
    val reachMonth: YearMonth?,
    val deadlineMonth: YearMonth?,
    /** > 0 — запас в месяцах, < 0 — опоздание. */
    val slackMonths: Int?,
    val neededPerMonthMinor: Long?,
)

object GoalPlanner {
    const val PACE_MIN = 2_000_000L
    const val PACE_MAX = 30_000_000L
    const val PACE_STEP = 500_000L

    fun plan(remaining: Long, pace: Long, deadline: LocalDate?, today: LocalDate): GoalPlan {
        val now = YearMonth.from(today)
        val months = if (remaining <= 0) 0L else if (pace <= 0) null else (remaining + pace - 1) / pace
        val reach = months?.let { now.plusMonths(it) }
        val deadlineMonth = deadline?.let { YearMonth.from(it) }
        val slack = if (reach != null && deadlineMonth != null) ChronoUnit.MONTHS.between(reach, deadlineMonth).toInt() else null
        val monthsToDeadline = deadlineMonth?.let { ChronoUnit.MONTHS.between(now, it).coerceAtLeast(1) }
        val needed = monthsToDeadline?.let { (remaining + it - 1) / it }
        return GoalPlan(pace, reach, deadlineMonth, slack, needed)
    }

    /** Темп по умолчанию — среднее пополнение за последние 3 месяца, в пределах слайдера. */
    fun defaultPace(deposits: List<GoalDeposit>, today: LocalDate): Long {
        val from = today.minusMonths(3)
        val sum = deposits.filter { DateFmt.toLocalDate(it.createdAtMillis).isAfter(from) }.sumOf { it.amountMinor }
        val avg = sum / 3
        val snapped = ((avg + PACE_STEP / 2) / PACE_STEP) * PACE_STEP
        return snapped.coerceIn(PACE_MIN, PACE_MAX)
    }
}

data class GoalDetailUiState(
    val goal: Goal? = null,
    val currencyCode: String = "RUB",
    val paceMinor: Long = GoalPlanner.PACE_MIN,
    val plan: GoalPlan? = null,
    val deleted: Boolean = false,
)

class GoalDetailViewModel(
    private val goalId: Long,
    private val goalRepository: GoalRepository,
    settingsRepository: SettingsRepository,
    private val context: Context,
) : ViewModel() {

    private val pace = MutableStateFlow<Long?>(null)
    private val _deleted = MutableStateFlow(false)
    private val _celebrate = MutableStateFlow(false)
    val celebrate: StateFlow<Boolean> = _celebrate.asStateFlow()
    private var selectedAccountId: Long = 1L

    val uiState: StateFlow<GoalDetailUiState> = combine(
        goalRepository.observeGoal(goalId),
        goalRepository.observeDeposits(goalId),
        settingsRepository.settings,
        pace,
        _deleted,
    ) { goal, deposits, settings, p, deleted ->
        selectedAccountId = settings.selectedAccountId
        val today = LocalDate.now()
        val effectivePace = p ?: GoalPlanner.defaultPace(deposits, today)
        GoalDetailUiState(
            goal = goal,
            currencyCode = goal?.currencyCode ?: settings.defaultCurrencyCode,
            paceMinor = effectivePace,
            plan = goal?.let { GoalPlanner.plan(it.remainingMinor, effectivePace, it.deadline?.let(DateFmt::toLocalDate), today) },
            deleted = deleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalDetailUiState())

    fun setPace(value: Long) {
        pace.value = value
    }

    /** Быстрое пополнение (§6.9 п.4). */
    fun quickDeposit(amountMinor: Long) {
        val goal = uiState.value.goal ?: return
        val amount = amountMinor.coerceAtMost(goal.remainingMinor)
        if (amount <= 0) return
        viewModelScope.launch {
            val wasCompleted = goal.isCompleted
            runCatching {
                goalRepository.addDeposit(
                    goalId = goalId,
                    amountMinor = amount,
                    note = null,
                    currencyCode = uiState.value.currencyCode,
                    accountId = selectedAccountId,
                    transactionNote = context.getString(R.string.goal_deposit_transaction_note_prefix, goal.name),
                )
            }.onSuccess { updated -> if (!wasCompleted && updated.isCompleted) _celebrate.value = true }
        }
    }

    fun consumeCelebration() {
        _celebrate.value = false
    }

    fun setShowOnHome(show: Boolean) {
        val goal = uiState.value.goal ?: return
        viewModelScope.launch { goalRepository.upsertGoal(goal.copy(showOnHome = show)) }
    }

    fun delete() {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            _deleted.value = true
        }
    }
}
