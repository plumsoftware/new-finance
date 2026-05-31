package ru.plumsoftware.finance.domain.model

data class Insight(
    val type: InsightType,
    val categoryId: Long? = null,
    val transactionId: Long? = null,
    val value: Number = 0,
    val severity: InsightSeverity = InsightSeverity.LOW,
)

enum class InsightType {
    CATEGORY_SPIKE,
    GOOD_SAVINGS,
    LARGE_EXPENSE,
    STREAK,
}

enum class InsightSeverity {
    LOW,
    MEDIUM,
    HIGH,
}
