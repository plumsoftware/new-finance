package ru.plumsoftware.finance.presentation.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.budget.BudgetService
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.AmountInputMatcher
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.TxItem
import ru.plumsoftware.finance.presentation.common.toTxItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class HistoryFilter { ALL, EXPENSES, INCOME }

data class WeekBar(val date: LocalDate, val totalMinor: Long, val isToday: Boolean, val aboveNorm: Boolean, val isFuture: Boolean)

data class DayGroup(val date: LocalDate, val expensesMinor: Long, val items: List<TxItem>)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val currencyCode: String = "RUB",
    val weekStart: LocalDate = LocalDate.now(),
    val weekEnd: LocalDate = LocalDate.now(),
    val weekTotal: Long = 0,
    val weekBars: List<WeekBar> = emptyList(),
    val query: String = "",
    val filter: HistoryFilter = HistoryFilter.ALL,
    val groups: List<DayGroup> = emptyList(),
    val hasAny: Boolean = false,
)

/** История (§6.3). Хранит всю историю операций — без автоудаления старых записей. */
class HistoryViewModel(
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    categoryRepository: CategoryRepository,
    settingsRepository: SettingsRepository,
    accountRepository: AccountRepository,
    private val context: Context,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(HistoryFilter.ALL)

    private val categories = combine(
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
        categoryRepository.observeByType(CategoryType.INCOME, includeHidden = true),
    ) { e, i -> e + i }

    val uiState: StateFlow<HistoryUiState> = combine(
        transactionRepository.observeAll(),
        categories,
        combine(settingsRepository.settings, accountRepository.observeAllActive()) { s, a -> s to a },
        query,
        filter,
    ) { tx, cats, (settings, accounts), q, f ->
        val today = LocalDate.now()
        val catMap = cats.associateBy { it.id }
        val accMap = accounts.associateBy { it.id }
        val savings = context.getString(R.string.home_savings_to_goal)
        val noCat = context.getString(R.string.home_no_category)
        val items = tx.sortedByDescending { it.dateMillis }.map { it.toTxItem(catMap, accMap, savings, noCat) }

        // Неделя Пн–Вс.
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(6)
        val budget = BudgetService.compute(tx, cats, settings.monthlyBudgetMinor, today, DateFmt::toLocalDate)?.budget
        val norm = budget?.let { it.budget / it.daysInMonth }
        val expenseByDate = items.filter { it.type == TransactionType.EXPENSE }.groupBy { it.date }
            .mapValues { e -> e.value.sumOf { it.amountMinor } }
        val bars = (0..6).map { i ->
            val d = monday.plusDays(i.toLong())
            val total = expenseByDate[d] ?: 0L
            WeekBar(d, total, d == today, norm != null && total > norm, d.isAfter(today))
        }

        val filtered = items.filter { item ->
            when (f) {
                HistoryFilter.ALL -> true
                HistoryFilter.EXPENSES -> item.type == TransactionType.EXPENSE
                HistoryFilter.INCOME -> item.type == TransactionType.INCOME
            }
        }.filter { matches(it, q) }

        val groups = filtered.groupBy { it.date }.map { (date, list) ->
            DayGroup(date, list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }, list)
        }.sortedByDescending { it.date }

        HistoryUiState(
            isLoading = false,
            currencyCode = settings.defaultCurrencyCode,
            weekStart = monday,
            weekEnd = sunday,
            weekTotal = bars.sumOf { it.totalMinor },
            weekBars = bars,
            query = q,
            filter = f,
            groups = groups,
            hasAny = items.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    /** Поиск по названию, категории, заметке и сумме. */
    private fun matches(item: TxItem, q: String): Boolean {
        val query = q.trim()
        if (query.isEmpty()) return true
        val lower = query.lowercase()
        return item.title.lowercase().contains(lower) ||
            item.categoryName.lowercase().contains(lower) ||
            item.note.orEmpty().lowercase().contains(lower) ||
            AmountInputMatcher.matches(item.amountMinor, query)
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun setFilter(f: HistoryFilter) {
        filter.value = f
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getById(id)
            if (transaction?.type == TransactionType.SAVINGS && transaction.goalId != null) {
                goalRepository.deleteDepositByTransactionId(id)
            } else {
                transactionRepository.delete(id)
            }
        }
    }
}
