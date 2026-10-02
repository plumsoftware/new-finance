package ru.plumsoftware.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.plumsoftware.finance.domain.analytics.AnalyticsMath
import ru.plumsoftware.finance.domain.analytics.BucketUnit
import ru.plumsoftware.finance.domain.analytics.DateRange
import ru.plumsoftware.finance.domain.analytics.PeriodMode
import ru.plumsoftware.finance.domain.budget.BudgetMath
import ru.plumsoftware.finance.domain.calculators.Calc
import ru.plumsoftware.finance.domain.insights.KopiTip
import ru.plumsoftware.finance.domain.insights.KopiTips
import ru.plumsoftware.finance.domain.model.Goal
import ru.plumsoftware.finance.domain.model.remainingMinor
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.common.plural
import ru.plumsoftware.finance.ui.ds.AmountInput
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

class RedesignLogicTest {

    private val nb = ' '

    init {
        Locale.setDefault(Locale("ru", "RU"))
    }

    // §11 Форматы

    @Test
    fun money_format() {
        assertEquals("1${nb}234${nb}567,89$nb₽", Money.format(123_456_789))
        assertEquals("25${nb}000$nb₽", Money.format(2_500_000))
        assertEquals("990,00$nb₽", Money.format(99_000, forceFraction = true))
        assertEquals("−990$nb₽", Money.signed(99_000, isIncome = false))
        assertEquals("+25${nb}000$nb₽", Money.signed(2_500_000, isIncome = true))
    }

    @Test
    fun money_short() {
        assertEquals("1,2к", Money.short(120_000))
        assertEquals("3,5${nb}млн", Money.short(350_000_000))
        assertEquals("990", Money.short(99_000))
        assertEquals("2к", Money.short(200_000))
    }

    @Test
    fun plural_rules() {
        assertEquals("цель", plural(1, "цель", "цели", "целей"))
        assertEquals("цели", plural(2, "цель", "цели", "целей"))
        assertEquals("целей", plural(0, "цель", "цели", "целей"))
        assertEquals("целей", plural(11, "цель", "цели", "целей"))
        assertEquals("цель", plural(21, "цель", "цели", "целей"))
        assertEquals("целей", plural(112, "цель", "цели", "целей"))
        assertEquals("лет", plural(5, "год", "года", "лет"))
    }

    // §2 п.1

    @Test
    fun goal_remaining_is_target_minus_saved() {
        val g = Goal(
            name = "Квартира", emoji = "🏠", targetAmountMinor = 1_600_000_000, savedAmountMinor = 325_000_000,
            colorHex = "#007AFF", deadline = null, note = null, showOnHome = true, isCompleted = false, createdAtMillis = 0,
        )
        assertEquals(1_275_000_000L, g.remainingMinor)
    }

    // §6.2 Клавиатура

    @Test
    fun amount_input_limits() {
        var s = ""
        "1234567890".forEach { s = AmountInput.press(s, it.toString()) }
        assertEquals("123456789", s)
        s = AmountInput.press(s, ",")
        s = AmountInput.press(s, ",")
        "567".forEach { s = AmountInput.press(s, it.toString()) }
        assertEquals("123456789,56", s)
        assertEquals(12_345_678_956L, AmountInput.toMinor(s))
        assertEquals("0,", AmountInput.press("", ","))
        assertEquals("5", AmountInput.press("0", "5"))
        assertEquals("990", AmountInput.fromMinor(99_000))
        assertEquals("12,5", AmountInput.fromMinor(1_250))
    }

    // §8.1

    @Test
    fun daily_budget_formulas() {
        val today = LocalDate.of(2026, 9, 27)
        // 26 дней по 1 000 ₽ до сегодня, сегодня 500 ₽, бюджет 40 000 ₽.
        val byDay = (1..26).associateWith { 100_000L } + (27 to 50_000L)
        val b = BudgetMath.compute(4_000_000, byDay, today)
        assertEquals(2_600_000, b.spentBefore)
        assertEquals(4, b.daysLeft)
        assertEquals((4_000_000L - 2_600_000L) / 4, b.daily)
        assertEquals(b.daily - 50_000, b.left)
        // forecast = round(26 500 / 27 × 30, 100) = 29 444 → 29 400
        assertEquals(2_940_000, b.forecast)
        assertEquals(4_000_000 - 2_940_000, b.diff)
    }

    @Test
    fun day_colors_and_limits() {
        val today = LocalDate.of(2026, 9, 3)
        val colors = BudgetMath.dayColors(3_000_000, mapOf(1 to 50_000L, 2 to 200_000L), today)
        assertEquals(30, colors.size)
        assertEquals(BudgetMath.DayColor.NORMAL, colors[0])
        assertEquals(BudgetMath.DayColor.OVER, colors[1])
        assertEquals(BudgetMath.DayColor.TODAY, colors[2])
        assertEquals(BudgetMath.DayColor.FUTURE, colors[3])
        assertEquals(1_300_000L, BudgetMath.suggestLimit(1_050_000)) // 12 600 → 13 000
        assertEquals(1_200_000L, BudgetMath.suggestLimit(1_000_000))
        assertEquals(BudgetMath.LimitStatus.ALMOST, BudgetMath.limitStatus(80, 100))
        assertEquals(BudgetMath.LimitStatus.EXCEEDED, BudgetMath.limitStatus(100, 100))
    }

    // §8.3

    @Test
    fun analytics_ranges() {
        val today = LocalDate.of(2026, 9, 27) // воскресенье
        val week = AnalyticsMath.range(PeriodMode.WEEK, today)
        assertEquals(LocalDate.of(2026, 9, 21), week.start)
        val month = AnalyticsMath.range(PeriodMode.MONTH, today)
        assertEquals(BucketUnit.DAY, AnalyticsMath.bucketUnit(PeriodMode.MONTH, month))
        val prev = AnalyticsMath.previousRange(PeriodMode.MONTH, month, today)!!
        assertEquals(DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 27)), prev)
        val custom = DateRange(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10))
        assertEquals(
            DateRange(LocalDate.of(2026, 8, 22), LocalDate.of(2026, 8, 31)),
            AnalyticsMath.previousRange(PeriodMode.CUSTOM, custom, today),
        )
        assertEquals(-6, AnalyticsMath.comparePercent(9_400, 10_000))
        assertNull(AnalyticsMath.comparePercent(100, 0))
        val long = DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 27))
        assertEquals(BucketUnit.MONTH, AnalyticsMath.bucketUnit(PeriodMode.CUSTOM, long))
        assertEquals(9, AnalyticsMath.buckets(long, BucketUnit.MONTH, emptyMap()).size)
    }

    // §8.5

    private fun near(expected: Double, actual: Double, eps: Double = 1.0) =
        assertTrue("expected $expected, got $actual", abs(expected - actual) <= eps)

    @Test
    fun calc_credit() {
        // 1 000 000 ₽, 12%, 12 мес. → 88 848,79 ₽
        val r = Calc.credit(1_000_000.0, 12.0, 12)
        near(88_848.79, r.payment, 0.01)
        near(r.payment * 12 - 1_000_000, r.overpay, 0.01)
        near(100_000.0 / 12, Calc.ann(100_000.0, 0.0, 12), 0.001)
    }

    @Test
    fun calc_deposit() {
        // 100 000 ₽, 12%, 12 мес., ежемесячная капитализация → 112 682,50 ₽
        val r = Calc.deposit(100_000.0, 0.0, 12.0, 12, Calc.Capitalization.MONTHLY)
        near(112_682.50, r.finalAmount)
        val simple = Calc.deposit(100_000.0, 0.0, 12.0, 12, Calc.Capitalization.NONE)
        near(112_000.0, simple.finalAmount)
    }

    @Test
    fun calc_goal() {
        val r = Calc.goal(120_000.0, 0.0, 12, 0.0)
        near(10_000.0, r.monthly, 0.001)
        val withYield = Calc.goal(1_000_000.0, 100_000.0, 24, 10.0)
        assertEquals(0.0, withYield.monthly % 100, 0.0)
    }

    @Test
    fun calc_mortgage_family() {
        val market = Calc.mortgage(10_000_000.0, 20.0, 20.0, 20, family = false)
        val family = Calc.mortgage(10_000_000.0, 20.0, 20.0, 20, family = true)
        near(Calc.ann(8_000_000.0, 20.0, 240), market.payment, 0.001)
        near(Calc.ann(6_000_000.0, 6.0, 240) + Calc.ann(2_000_000.0, 20.0, 240), family.payment, 0.001)
    }

    @Test
    fun calc_early_repayment() {
        val none = Calc.earlyRepayment(1_000_000.0, 12.0, 120, 0.0, Calc.EarlyMode.REDUCE_TERM)
        near(0.0, none.savedInterest)
        assertEquals(0, none.monthsSaved)
        val term = Calc.earlyRepayment(1_000_000.0, 12.0, 120, 10_000.0, Calc.EarlyMode.REDUCE_TERM)
        assertTrue(term.monthsSaved > 0 && term.savedInterest > 0)
        val pay = Calc.earlyRepayment(1_000_000.0, 12.0, 120, 10_000.0, Calc.EarlyMode.REDUCE_PAYMENT)
        assertTrue(pay.paymentAfterYear < pay.basePayment && pay.savedInterest > 0)
    }

    @Test
    fun kopi_tip_priority() {
        val today = LocalDate.of(2026, 9, 27)
        val over = BudgetMath.compute(3_000_000, mapOf(27 to 1_000_000L), today)
        val tip = KopiTips.pick(over, emptyList(), emptyList(), emptyList(), 0, -1, -1)
        assertTrue(tip is KopiTip.DailyExceeded)
        val neutral = KopiTips.pick(null, emptyList(), emptyList(), emptyList(), 100, 3, 99)
        assertEquals(KopiTip.Neutral(4), neutral)
    }
}
