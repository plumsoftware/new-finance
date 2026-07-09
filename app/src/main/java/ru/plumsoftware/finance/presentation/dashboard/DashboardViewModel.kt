package ru.plumsoftware.finance.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.CategoryBudgetSpending
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.Insight
import ru.plumsoftware.finance.domain.model.MonthPeriod
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.StreakData
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.insights.InsightsEngine
import ru.plumsoftware.finance.data.local.dao.AchievementUnlockDao
import ru.plumsoftware.finance.data.local.entity.AchievementUnlockEntity
import ru.plumsoftware.finance.data.repository.StreakRepository
import ru.plumsoftware.finance.domain.model.AccountWithBalance
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.GoalRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.SmartAssetRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.data.util.endOfDayMillis
import ru.plumsoftware.finance.data.util.startOfDayMillis
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Account

data class DashboardUiState(
    val totalBalanceMinor: Long = 0L,
    val currencyCode: String = "RUB",
    val monthIncomeMinor: Long = 0L,
    val monthExpenseMinor: Long = 0L,
    val todayOperationsCount: Int = 0,
    val categoryMap: Map<Long, Category> = emptyMap(),
    val smartAssets: List<SmartAsset> = emptyList(),
    val featuredGoal: Goal? = null,
    val streak: StreakData = StreakData(),
    val insights: List<Insight> = emptyList(),
    val hasBudgetWarnings: Boolean = false,
    val unlockedAchievementsCount: Int = 0,
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null,
    val accountsData: List<AccountDashboardData> = emptyList(),
    val totalGoalSavingsMinor: Long = 0L,
    val activeGoalsCount: Int = 0
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val smartAssetRepository: SmartAssetRepository,
    private val goalRepository: GoalRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val insightsEngine: InsightsEngine,
    private val streakRepository: StreakRepository,
    private val achievementUnlockDao: AchievementUnlockDao,
    private val context: Context,
) : ViewModel() {

    private val _snackbar = MutableStateFlow<String?>(null)
    private val _warningDismissed = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        transactionRepository.observeAll(),
        accountRepository.observeAllWithBalances(),
        smartAssetRepository.observeByStatus(SmartAssetStatus.PAYING_OFF),
        goalRepository.observeFeaturedOnHome(),
        categoryRepository.observeByType(CategoryType.EXPENSE, includeHidden = true),
        categoryRepository.observeByType(CategoryType.INCOME, includeHidden = true),
        categoryRepository.getCategoryWithSpending(MonthPeriod.current()),
        settingsRepository.settings,
        streakRepository.observe(),
        achievementUnlockDao.observeAll(),
        goalRepository.observeGoals(),
        _snackbar,
        _warningDismissed,
    ) { values ->
        val transactions = values[0] as List<Transaction>
        val accountBalances = values[1] as List<AccountWithBalance>
        val assets = values[2] as List<SmartAsset>
        val featuredGoal = values[3] as Goal?
        val expenseCategories = values[4] as List<Category>
        val incomeCategories = values[5] as List<Category>
        val budgetSpending = values[6] as List<CategoryBudgetSpending>
        val settings = values[7] as ru.plumsoftware.finance.domain.model.AppSettings
        val streak = values[8] as StreakData

        @Suppress("UNCHECKED_CAST")
        val achievementUnlocks = values[9] as List<AchievementUnlockEntity>

        @Suppress("UNCHECKED_CAST")
        val allGoals = values[10] as List<Goal> // Извлечение всех целей

        val snackbar = values[11] as String?
        val warningDismissed = values[12] as Boolean

        val monthRange = currentMonthRange()
        val previousRange = previousMonthRange()

        // Генерация данных для каждого счета по отдельности (для карусели)
        val accountsData = accountBalances.map { accWithBalance ->
            val acc = accWithBalance.account
            val accTx = transactions.filter { it.accountId == acc.id }
            val accMonthTx = accTx.filter { it.dateMillis in monthRange.first..monthRange.second }

            val inc =
                accMonthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
            val exp =
                accMonthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }

            AccountDashboardData(
                accountId = acc.id,
                name = acc.name,
                balanceMinor = accWithBalance.calculatedBalanceMinor,
                currencyCode = acc.currencyCode,
                monthIncomeMinor = inc,
                monthExpenseMinor = exp,
                isSavings = isSavingsAccount(acc)
            )
        }

        val totalGoalSavings = allGoals.sumOf { it.savedAmountMinor }

        val selectedAccount = accountBalances.find { it.account.id == settings.selectedAccountId }
            ?: accountBalances.firstOrNull()
        val accountTransactions = transactions.filter { it.accountId == settings.selectedAccountId }

        val monthTx =
            accountTransactions.filter { it.dateMillis in monthRange.first..monthRange.second }
        val previousMonthTx =
            accountTransactions.filter { it.dateMillis in previousRange.first..previousRange.second }

        val todayStart = startOfDayMillis(System.currentTimeMillis())
        val todayEnd = endOfDayMillis(System.currentTimeMillis())
        val todayOperationsCount = accountTransactions.count { tx ->
            tx.dateMillis >= todayStart && tx.dateMillis < todayEnd
        }
        val insights = insightsEngine.generateInsights(monthTx, previousMonthTx)
        val totalBalance = selectedAccount?.calculatedBalanceMinor ?: 0L
        val hasBudgetWarnings = !warningDismissed && budgetSpending.any { item ->
            val limit = item.limitMinor
            limit != null && limit > 0L && item.percentage >= 0.8f
        }

        val activeGoalsCount = allGoals.count { !it.isCompleted }

        DashboardUiState(
            totalBalanceMinor = totalBalance,
            currencyCode = selectedAccount?.account?.currencyCode ?: settings.defaultCurrencyCode,
            monthIncomeMinor = monthTx.filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountMinor },
            monthExpenseMinor = monthTx.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountMinor },
            todayOperationsCount = todayOperationsCount,
            categoryMap = (expenseCategories + incomeCategories).associateBy { it.id },
            smartAssets = assets,
            featuredGoal = featuredGoal,
            streak = streak,
            insights = insights,
            hasBudgetWarnings = hasBudgetWarnings,
            unlockedAchievementsCount = achievementUnlocks.size,
            isLoading = false,
            snackbarMessage = snackbar,
            accountsData = accountsData,
            totalGoalSavingsMinor = totalGoalSavings,
            activeGoalsCount = activeGoalsCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        viewModelScope.launch {
            transactionRepository.observeAll().collect {
                streakRepository.calculateAndSave()
            }
        }
    }

    // Безопасное определение накопительного счета с помощью рефлексии
    private fun isSavingsAccount(account: Account): Boolean {
        // Использование стандартной Java Reflection (всегда доступно в Android без дополнительных зависимостей)
        try {
            val methods = account.javaClass.methods

            // 1. Попытка найти булевы геттеры свойств "isSavings" или "isSavingsAccount"
            val savingsMethod = methods.find { method ->
                method.name == "isSavings" ||
                        method.name == "isSavingsAccount" ||
                        method.name == "getIsSavings" ||
                        method.name == "getIsSavingsAccount"
            }
            if (savingsMethod != null) {
                val res = savingsMethod.invoke(account)
                if (res is Boolean) return res
            }
        } catch (e: Exception) {
            // Игнорируем исключения при попытке доступа
        }

        try {
            val methods = account.javaClass.methods

            // 2. Попытка проверить тип аккаунта (например, свойство "type" или "accountType")
            val typeMethod = methods.find { method ->
                method.name == "getType" ||
                        method.name == "getAccountType" ||
                        method.name == "type"
            }
            if (typeMethod != null) {
                val res = typeMethod.invoke(account)?.toString()?.uppercase()
                if (res != null && (res.contains("SAVING") || res.contains("SAVINGS"))) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Игнорируем исключения при попытке доступа
        }

        // 3. Резервный поиск по названию счета (свойство "name" гарантированно доступно при компиляции)
        val nameLower = account.name.lowercase()
        return nameLower.contains("накоп") || nameLower.contains("сберег") || nameLower.contains("saving")
    }

    fun recordSmartUsage(assetId: Long) {
        viewModelScope.launch {
            runCatching {
                smartAssetRepository.recordUsage(smartAssetId = assetId)
            }
                .onSuccess {
                    _snackbar.value = context.getString(R.string.smart_usage_saved)
                }
                .onFailure {
                    _snackbar.value = it.message ?: context.getString(R.string.error_save_failed)
                }
        }
    }

    fun clearSnackbar() {
        _snackbar.value = null
    }

    fun dismissWarning() {
        _warningDismissed.value = true
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getById(id)
            if (transaction?.type == TransactionType.SAVINGS && transaction.goalId != null) {
                goalRepository.deleteDepositByTransactionId(id)
            } else {
                transactionRepository.delete(id)
            }
            _snackbar.value = context.getString(R.string.transaction_deleted)
        }
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val now = Calendar.getInstance()
        val start = now.clone() as Calendar
        start.set(Calendar.DAY_OF_MONTH, 1)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        val end = start.clone() as Calendar
        end.add(Calendar.MONTH, 1)
        end.add(Calendar.MILLISECOND, -1)
        return start.timeInMillis to end.timeInMillis
    }

    private fun previousMonthRange(): Pair<Long, Long> {
        val (startMillis, endExclusiveMillis) = MonthPeriod.previous().toMillisRange()
        return startMillis to (endExclusiveMillis - 1)
    }
}
