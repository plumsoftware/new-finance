package ru.plumsoftware.finance.domain.model

enum class LimitStatus {
    NONE,
    OK,
    WARNING,
    EXCEEDED,
}

data class CategoryWithSpending(
    val category: Category,
    val spentThisMonth: Double,
    val limit: Double?,
) {
    val progress: Float
        get() = if (limit != null && limit > 0) {
            (spentThisMonth / limit).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    val status: LimitStatus
        get() = when {
            limit == null -> LimitStatus.NONE
            progress >= 1f -> LimitStatus.EXCEEDED
            progress >= 0.8f -> LimitStatus.WARNING
            else -> LimitStatus.OK
        }
}
