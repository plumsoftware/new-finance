package ru.plumsoftware.finance.data.repository

import kotlinx.coroutines.flow.first
import ru.plumsoftware.finance.data.local.dao.AccountDao
import ru.plumsoftware.finance.data.local.dao.SmartAssetDao
import ru.plumsoftware.finance.data.local.dao.TransactionDao
import ru.plumsoftware.finance.data.mapper.toDomain
import ru.plumsoftware.finance.domain.model.AccountAnalytics
import ru.plumsoftware.finance.domain.model.PeriodSummary
import ru.plumsoftware.finance.domain.model.SavingsIndex
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.AnalyticsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import java.util.concurrent.TimeUnit

class AnalyticsRepositoryImpl(
    private val transactionRepository: TransactionRepository,
    private val smartAssetDao: SmartAssetDao,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
) : AnalyticsRepository {

    override suspend fun getPeriodSummary(startMillis: Long, endMillis: Long): PeriodSummary {
        val income = transactionRepository.sumByTypeInPeriod(
            TransactionType.INCOME,
            startMillis,
            endMillis,
        )
        val expense = transactionRepository.sumByTypeInPeriod(
            TransactionType.EXPENSE,
            startMillis,
            endMillis,
        )
        val savings = transactionRepository.sumByTypeInPeriod(
            TransactionType.SAVINGS,
            startMillis,
            endMillis,
        )
        return PeriodSummary(
            incomeMinor = income,
            expenseMinor = expense,
            savingsMinor = savings,
            netMinor = income - expense,
        )
    }

    override suspend fun getSavingsIndex(startMillis: Long, endMillis: Long): SavingsIndex {
        val totalSmartSavings = smartAssetDao.observeTotalSavedAllTime().first()
        val periodDays = ((endMillis - startMillis).coerceAtLeast(1L) / TimeUnit.DAYS.toMillis(1))
            .coerceAtLeast(1L)
        val estimatedMonthly = totalSmartSavings * 30L / periodDays
        return SavingsIndex(
            totalSmartSavingsMinor = totalSmartSavings,
            estimatedMonthlyReductionMinor = estimatedMonthly,
        )
    }

    override suspend fun getAccountAnalytics(
        startMillis: Long,
        endMillis: Long,
    ): List<AccountAnalytics> {
        val statsList = transactionDao.getStatsByAccount(startMillis, endMillis)
        if (statsList.isEmpty()) return emptyList()

        val accounts = accountDao.getAllActiveSync().map { it.toDomain() }
        val balances = accountDao.observeAllWithBalances().first()
            .associate { row -> row.account.id to row.calculatedBalance }

        val totalIncome = statsList.sumOf { it.totalIncome }.coerceAtLeast(1L)
        val totalExpense = statsList.sumOf { it.totalExpense }.coerceAtLeast(1L)

        return statsList.mapNotNull { stats ->
            val account = accounts.find { it.id == stats.accountId } ?: return@mapNotNull null
            AccountAnalytics(
                account = account,
                incomeMinor = stats.totalIncome,
                expenseMinor = stats.totalExpense,
                balanceMinor = balances[stats.accountId] ?: account.initialBalanceMinor,
                transactionCount = stats.txCount,
                incomeShare = stats.totalIncome.toFloat() / totalIncome,
                expenseShare = stats.totalExpense.toFloat() / totalExpense,
            )
        }.sortedByDescending { it.expenseMinor + it.incomeMinor }
    }
}
