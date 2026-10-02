package ru.plumsoftware.finance.data.report

import ru.plumsoftware.finance.domain.analytics.DateRange
import java.time.LocalDate
import java.time.LocalDateTime

/** Формат выгрузки (§6.6). */
enum class ReportFormat(val extension: String, val mimeType: String) {
    PDF("pdf", "application/pdf"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("csv", "text/csv"),
}

/** Отчёт из Аналитики или полный экспорт данных из Настроек. */
enum class ReportKind { REPORT, EXPORT }

data class ReportRequest(
    val kind: ReportKind,
    val format: ReportFormat,
    val range: DateRange,
    /** Режим периода — для имени файла и подписи («сентябрь 2026»). */
    val isWholeMonth: Boolean,
    val excludedCategoryIds: Set<Long> = emptySet(),
    val includeChart: Boolean = true,
    val includeCategories: Boolean = true,
    val includeOperations: Boolean = true,
)

data class ReportBar(val label: String, val valueMinor: Long, val isCurrent: Boolean)

data class ReportCategory(val name: String, val emoji: String, val colorArgb: Int, val amountMinor: Long, val share: Double)

data class ReportOperation(
    val dateTime: LocalDateTime,
    val title: String,
    val category: String,
    val isIncome: Boolean,
    val account: String,
    val amountMinor: Long,
    val note: String?,
)

data class Report(
    val request: ReportRequest,
    val title: String,
    val periodLabel: String,
    val generatedAt: LocalDateTime,
    val currencyCode: String,
    val totalExpenseMinor: Long,
    val totalIncomeMinor: Long,
    val comparePercent: Int?,
    val bars: List<ReportBar>,
    val categories: List<ReportCategory>,
    val operations: List<ReportOperation>,
    val excludedNames: List<String>,
) {
    val diffMinor: Long get() = totalIncomeMinor - totalExpenseMinor
}

/** Имена файлов (§9): `Отчёт_сентябрь_2026.pdf`, `Отчёт_1–27_сентября.xlsx`, `Финансы_экспорт_27.09.2026.csv`. */
object ReportFileNames {
    fun build(
        kind: ReportKind,
        format: ReportFormat,
        range: DateRange,
        isWholeMonth: Boolean,
        today: LocalDate,
        reportPrefix: String,
        exportPrefix: String,
        monthYear: (LocalDate) -> String,
        rangeLabel: (LocalDate, LocalDate) -> String,
        numericDate: (LocalDate) -> String,
    ): String {
        val base = when (kind) {
            ReportKind.EXPORT -> "${exportPrefix}_${numericDate(today)}"
            ReportKind.REPORT -> if (isWholeMonth) {
                "${reportPrefix}_${monthYear(range.start)}"
            } else {
                "${reportPrefix}_${rangeLabel(range.start, range.end)}"
            }
        }
        return base.replace(' ', '_').replace(' ', '_').replace("/", "-") + "." + format.extension
    }
}
