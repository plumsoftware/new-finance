package ru.plumsoftware.finance.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.repository.StreakRepository
import ru.plumsoftware.finance.domain.achievements.AchievementId
import ru.plumsoftware.finance.domain.budget.BudgetMath
import ru.plumsoftware.finance.domain.budget.BudgetService
import ru.plumsoftware.finance.domain.budget.DailyBudget
import ru.plumsoftware.finance.domain.budget.Upcoming
import ru.plumsoftware.finance.domain.budget.UpcomingCharge
import ru.plumsoftware.finance.domain.insights.KopiTip
import ru.plumsoftware.finance.domain.insights.KopiTips
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.domain.model.AppSettings
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.RecurringTransaction
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.NotificationRepository
import ru.plumsoftware.finance.domain.repository.RecurringRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import ru.plumsoftware.finance.presentation.common.TxItem
import ru.plumsoftware.finance.presentation.common.toTxItem
import java.time.LocalDate
import java.time.YearMonth

data class UpcomingUi(val charge: UpcomingCharge, val emoji: String)

data class SmartSummary(val assets: Int, val uses: Int, val savedMinor: Long)

data class HomeUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val currencyCode: String = "RUB",
    val streak: Int = 0,
    val unreadNotifications: Int = 0,
    val budget: DailyBudget? = null,
    val dayColors: List<BudgetMath.DayColor> = emptyList(),
    val achievementsUnlocked: Int = 0,
    val tip: KopiTip? = null,
    val upcoming: List<UpcomingUi> = emptyList(),
    val upcomingMore: Int = 0,
    val upcomingSum: Long = 0,
    val goals: List<Goal> = emptyList(),
    val smart: SmartSummary = SmartSummary(0, 0, 0),
    val todayOps: List<TxItem> = emptyList(),
    val accountsCount: Int = 0,
    val totalBalanceMinor: Long = 0,
    val hasAnyTransactions: Boolean = false,
)

/**
 * Главная (§6.1). Hero-блок пересчитывается сразу после добавления операции,
 * т.к. все данные — потоки Room.
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    smartAssetRepository: SmartAssetRepository,
    private val goalRepository: GoalRepository,
    categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val streakRepository: StreakRepository,
    achievementUnlockDao: AchievementUnlockDao,
    recurringRepository: RecurringRepository,
    notificationRepository: NotificationRepository,
    private val context: Context,
) : ViewModel() {

    private val categoriesFlow = combine(
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
        categoryRepository.observeByType(CategoryType.INCOME, includeHidden = true),
    ) { e, i -> e + i }

    private data class Sources(
        val tx: List<Transaction>,
        val accounts: List<AccountWithBalance>,
        val assets: List<SmartAsset>,
        val goals: List<Goal>,
        val categories: List<Category>,
        val settings: AppSettings,
        val streak: Int,
        val unlocked: Int,
        val recurring: List<RecurringTransaction>,
        val unread: Int,
    )

    private val sources = combine(
        combine(
            transactionRepository.observeAll(),
            accountRepository.observeAllWithBalances(),
            smartAssetRepository.observeActive(),
            goalRepository.observeGoals(),
            categoriesFlow,
        ) { tx, acc, assets, goals, cats -> listOf(tx, acc, assets, goals, cats) },
        combine(
            settingsRepository.settings,
            streakRepository.observe(),
            achievementUnlockDao.observeAll(),
            recurringRepository.observeAll(),
            notificationRepository.observeUnreadCount(),
        ) { s, st, un, rec, unread -> listOf(s, st.currentStreak, un.count { it.key in AchievementId.keys }, rec, unread) },
    ) { a, b ->
        @Suppress("UNCHECKED_CAST")
        Sources(
            tx = a[0] as List<Transaction>,
            accounts = a[1] as List<AccountWithBalance>,
            assets = a[2] as List<SmartAsset>,
            goals = a[3] as List<Goal>,
            categories = a[4] as List<Category>,
            settings = b[0] as AppSettings,
            streak = b[1] as Int,
            unlocked = b[2] as Int,
            recurring = b[3] as List<RecurringTransaction>,
            unread = b[4] as Int,
        )
    }

    val uiState: StateFlow<HomeUiState> = sources.map { build(it) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        // Стрик учитывает операции и дни «Сегодня без трат» (§8.7).
        viewModelScope.launch {
            combine(
                transactionRepository.observeAll().map { it.size },
                settingsRepository.settings.map { it.noSpendEpochDays }.distinctUntilChanged(),
            ) { _, days -> days }.collect { days ->
                streakRepository.calculateAndSave(days.map { LocalDate.ofEpochDay(it) }.toSet())
            }
        }
        // Нейтральный совет запоминается, чтобы не повторяться чаще раза в неделю (§8.6 п.6).
        viewModelScope.launch {
            uiState.map { it.tip }.distinctUntilChanged().collect { tip ->
                if (tip is KopiTip.Neutral) {
                    val today = LocalDate.now().toEpochDay()
                    val s = settingsRepository.settings.first()
                    if (s.lastNeutralTipEpochDay != today || s.lastNeutralTipIndex != tip.index) {
                        settingsRepository.update { it.copy(lastNeutralTipIndex = tip.index, lastNeutralTipEpochDay = today) }
                    }
                }
            }
        }
    }

    private fun build(s: Sources): HomeUiState {
        val today = LocalDate.now()
        val ym = YearMonth.from(today)
        val catMap = s.categories.associateBy { it.id }
        val accMap = s.accounts.associate { it.account.id to it.account }

        val expenseDates = s.tx.filter { it.type == TransactionType.EXPENSE }.map { it to DateFmt.toLocalDate(it.dateMillis) }
        val computed = BudgetService.compute(s.tx, s.categories, s.settings.monthlyBudgetMinor, today, DateFmt::toLocalDate)
        val budget = computed?.budget
        val dayColors = if (computed != null) BudgetMath.dayColors(computed.budget.budget, computed.monthByDay, today) else emptyList()

        val upcomingAll = Upcoming.occurrences(s.recurring, today, 14, DateFmt::toLocalDate)
        val upcoming = upcomingAll.take(4).map { UpcomingUi(it, catMap[it.categoryId]?.icon ?: "🔁") }

        // Рост категорий: текущий месяц до сегодня против тех же дней прошлого месяца.
        val prevYm = ym.minusMonths(1)
        val prevEndDay = minOf(today.dayOfMonth, prevYm.lengthOfMonth())
        val growth = s.categories.filter { it.type == CategoryType.EXPENSE }.map { cat ->
            val cur = expenseDates.filter { it.first.categoryId == cat.id && YearMonth.from(it.second) == ym }.sumOf { it.first.amountMinor }
            val prev = expenseDates.filter {
                it.first.categoryId == cat.id && YearMonth.from(it.second) == prevYm && it.second.dayOfMonth <= prevEndDay
            }.sumOf { it.first.amountMinor }
            KopiTips.CategoryMonthSpend(cat.name, cur, prev)
        }
        // Окупился последним использованием: переплата меньше одной экономии.
        val paidOff = s.assets.filter {
            it.status == SmartAssetStatus.PROFIT && it.totalSavedMinor - it.purchaseCostMinor < it.alternativeCostMinor
        }.map { it.name }

        val tipHidden = s.settings.kopiTipHiddenEpochDay == today.toEpochDay()
        val tip = if (tipHidden) null else KopiTips.pick(
            budget = budget,
            categories = growth,
            upcoming = upcomingAll.map { KopiTips.UpcomingCharge(it.title, it.amountMinor, it.daysUntil) },
            paidOffAssets = paidOff,
            epochDay = today.toEpochDay(),
            lastNeutralIndex = s.settings.lastNeutralTipIndex,
            lastNeutralEpochDay = s.settings.lastNeutralTipEpochDay,
        )

        val savingsLabel = context.getString(R.string.home_savings_to_goal)
        val noCategory = context.getString(R.string.home_no_category)
        val todayOps = s.tx.filter { DateFmt.toLocalDate(it.dateMillis) == today }
            .sortedByDescending { it.dateMillis }
            .map { it.toTxItem(catMap, accMap, savingsLabel, noCategory) }

        return HomeUiState(
            isLoading = false,
            today = today,
            currencyCode = s.settings.defaultCurrencyCode,
            streak = s.streak,
            unreadNotifications = s.unread,
            budget = budget,
            dayColors = dayColors,
            achievementsUnlocked = s.unlocked,
            tip = tip,
            upcoming = upcoming,
            upcomingMore = (upcomingAll.size - upcoming.size).coerceAtLeast(0),
            upcomingSum = upcomingAll.sumOf { it.amountMinor },
            goals = s.goals.filter { it.showOnHome },
            smart = SmartSummary(s.assets.size, s.assets.sumOf { it.totalUses }, s.assets.sumOf { it.totalSavedMinor }),
            todayOps = todayOps,
            accountsCount = s.accounts.size,
            totalBalanceMinor = s.accounts.sumOf { it.calculatedBalanceMinor },
            hasAnyTransactions = s.tx.isNotEmpty(),
        )
    }

    /** Свайп совета в сторону — скрыт до следующего дня (§6.1.4). */
    fun hideTipForToday() {
        viewModelScope.launch {
            settingsRepository.update { it.copy(kopiTipHiddenEpochDay = LocalDate.now().toEpochDay()) }
        }
    }
}
