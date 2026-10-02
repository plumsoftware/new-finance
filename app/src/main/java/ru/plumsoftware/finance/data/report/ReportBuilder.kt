package ru.plumsoftware.finance.data.report

import android.content.Context
import kotlinx.coroutines.flow.first
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.analytics.AnalyticsMath
import ru.plumsoftware.finance.domain.analytics.BucketUnit
import ru.plumsoftware.finance.domain.analytics.DateRange
import ru.plumsoftware.finance.domain.analytics.PeriodMode
import ru.plumsoftware.finance.domain.model.CategoryType
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.domain.repository.AccountRepository
import ru.plumsoftware.finance.domain.repository.CategoryRepository
import ru.plumsoftware.finance.domain.repository.SettingsRepository
import ru.plumsoftware.finance.domain.repository.TransactionRepository
import ru.plumsoftware.finance.presentation.common.DateFmt
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Сборка данных отчёта (§9). Отчёт учитывает период и исключённые категории (§6.4);
 * полный экспорт данных исключения не учитывает.
 */
class ReportBuilder(
    private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun build(request: ReportRequest): Report {
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val settings = settingsRepository.settings.first()
        val categories = categoryRepository.observeByType(CategoryType.EXPENSE, true).first() +
            categoryRepository.observeByType(CategoryType.INCOME, true).first()
        val catMap = categories.associateBy { it.id }
        val accounts = accountRepository.observeAllWithBalances().first().associate { it.account.id to it.account.name }
        val excluded = if (request.kind == ReportKind.REPORT) request.excludedCategoryIds else emptySet()
        val all = transactionRepository.observeAll().first()

        fun included(categoryId: Long?) = categoryId == null || categoryId !in excluded
        val inRange = all.filter { DateFmt.toLocalDate(it.dateMillis) in request.range }

        val expenses = inRange.filter { it.type == TransactionType.EXPENSE && included(it.categoryId) }
        val incomes = inRange.filter { it.type == TransactionType.INCOME }
        val totalExpense = expenses.sumOf { it.amountMinor }

        // Сравнение с прошлым периодом (§8.3).
        val mode = if (request.isWholeMonth) PeriodMode.MONTH else PeriodMode.CUSTOM
        val prevRange = AnalyticsMath.previousRange(mode, request.range, today)
        val prevTotal = prevRange?.let { r ->
            all.filter {
                it.type == TransactionType.EXPENSE && included(it.categoryId) && DateFmt.toLocalDate(it.dateMillis) in r
            }.sumOf { it.amountMinor }
        } ?: 0L
        val compare = AnalyticsMath.comparePercent(totalExpense, prevTotal)

        // Столбцы графика.
        val byDate = expenses.groupBy { DateFmt.toLocalDate(it.dateMillis) }.mapValues { e -> e.value.sumOf { it.amountMinor } }
        val unit = AnalyticsMath.bucketUnit(mode, request.range)
        val buckets = AnalyticsMath.buckets(request.range, unit, byDate)
        val labelIdx = AnalyticsMath.axisLabelIndices(mode, unit, buckets)
        val bars = buckets.mapIndexed { i, b ->
            ReportBar(
                label = if (i in labelIdx) {
                    if (unit == BucketUnit.DAY) b.start.dayOfMonth.toString() else DateFmt.monthStandaloneShort(b.start)
                } else "",
                valueMinor = b.total,
                isCurrent = today in DateRange(b.start, b.end),
            )
        }

        val catShares = expenses.groupBy { it.categoryId }.map { (id, list) ->
            val cat = id?.let { catMap[it] }
            val sum = list.sumOf { it.amountMinor }
            ReportCategory(
                name = cat?.name ?: context.getString(R.string.home_no_category),
                emoji = cat?.icon ?: "💸",
                colorArgb = cat?.colorArgb?.toInt() ?: 0xFF007AFF.toInt(),
                amountMinor = sum,
                share = if (totalExpense > 0) sum.toDouble() / totalExpense else 0.0,
            )
        }.sortedByDescending { it.amountMinor }

        val ops = inRange
            .filter { it.type != TransactionType.EXPENSE || included(it.categoryId) }
            .sortedByDescending { it.dateMillis }
            .map { tx ->
                val cat = tx.categoryId?.let { catMap[it] }
                val catName = cat?.name ?: if (tx.type == TransactionType.SAVINGS) context.getString(R.string.home_savings_to_goal)
                else context.getString(R.string.home_no_category)
                ReportOperation(
                    dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.dateMillis), zone),
                    title = tx.note?.takeIf { it.isNotBlank() } ?: catName,
                    category = catName,
                    isIncome = tx.type == TransactionType.INCOME,
                    account = accounts[tx.accountId] ?: "",
                    amountMinor = tx.amountMinor,
                    note = tx.note,
                )
            }

        return Report(
            request = request,
            title = context.getString(if (request.kind == ReportKind.REPORT) R.string.report_title else R.string.report_export_title),
            periodLabel = periodLabel(request),
            generatedAt = LocalDateTime.now(),
            currencyCode = settings.defaultCurrencyCode,
            totalExpenseMinor = totalExpense,
            totalIncomeMinor = incomes.sumOf { it.amountMinor },
            comparePercent = compare,
            bars = bars,
            categories = catShares,
            operations = ops,
            excludedNames = excluded.mapNotNull { catMap[it]?.name }.sorted(),
        )
    }

    fun periodLabel(request: ReportRequest): String =
        if (request.isWholeMonth) DateFmt.monthYear(request.range.start)
        else DateFmt.range(request.range.start, request.range.end)
}
