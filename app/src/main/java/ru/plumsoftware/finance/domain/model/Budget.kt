package ru.plumsoftware.finance.domain.model

import java.util.Calendar

data class CategoryBudgetSpending(
    val category: Category,
    val spentMinor: Long,
    val limitMinor: Long?,
    val percentage: Float,
)

data class MonthPeriod(
    val year: Int,
    val month: Int,
) {
    fun toMillisRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endExclusive = start.clone() as Calendar
        endExclusive.add(Calendar.MONTH, 1)
        return start.timeInMillis to endExclusive.timeInMillis
    }

    companion object {
        fun current(): MonthPeriod {
            val now = Calendar.getInstance()
            return MonthPeriod(
                year = now.get(Calendar.YEAR),
                month = now.get(Calendar.MONTH) + 1,
            )
        }

        fun previous(): MonthPeriod {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -1)
            return MonthPeriod(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
            )
        }
    }
}
