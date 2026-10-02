package ru.plumsoftware.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.plumsoftware.finance.domain.receipt.FnsReceipt
import java.time.LocalDateTime

class FnsReceiptTest {
    @Test
    fun parsesStandardQr() {
        val r = FnsReceipt.parse("t=20260927T1315&s=990.50&fn=9287440300090728&i=12345&fp=3826178794&n=1")!!
        assertEquals(LocalDateTime.of(2026, 9, 27, 13, 15), r.dateTime)
        assertEquals(99_050L, r.amountMinor)
        assertEquals(1, r.operationType)
    }

    @Test
    fun parsesSecondsAndRejectsGarbage() {
        assertEquals(LocalDateTime.of(2026, 9, 27, 13, 15, 42), FnsReceipt.parse("t=20260927T131542&s=10&n=1")!!.dateTime)
        assertNull(FnsReceipt.parse("https://example.com"))
        assertNull(FnsReceipt.parse("t=2026&s=10"))
    }
}

class RecurringDatesTest {
    private val up = ru.plumsoftware.finance.domain.budget.Upcoming

    @Test
    fun firstMonthlyDate_isNearestChosenDay() {
        val today = LocalDateTime.of(2026, 10, 2, 0, 0).toLocalDate()
        assertEquals(today.withDayOfMonth(15), up.firstMonthlyDate(today, 15))
        assertEquals(today, up.firstMonthlyDate(today, 2))
        assertEquals(java.time.LocalDate.of(2026, 11, 1), up.firstMonthlyDate(today, 1))
        // 31-е в ноябре → 30 ноября
        assertEquals(java.time.LocalDate.of(2026, 11, 30), up.nextMonthly(java.time.LocalDate.of(2026, 10, 31), 31))
        assertEquals(java.time.LocalDate.of(2027, 3, 31), up.nextMonthly(java.time.LocalDate.of(2027, 2, 28), 31))
    }
}
