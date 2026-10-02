package ru.plumsoftware.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import ru.plumsoftware.finance.domain.calculators.Calc
import kotlin.math.pow

/**
 * Эталоны калькуляторов §8.5 (критерий приёмки §14 п.5: погрешность ≤ 1 ₽).
 * Значения получены независимым расчётом по формулам ТЗ и сверены с частными случаями.
 */
class CalculatorsReferenceTest {

    private fun near(expected: Double, actual: Double, eps: Double = 1.0) =
        assertEquals(expected, actual, eps)

    @Test
    fun savingsAccount_daily_and_minimum() {
        val daily = Calc.savingsAccount(300_000.0, 15.0, 12, 10_000.0, Calc.AccrualMode.DAILY)
        near(38_818.97, daily.income)
        near(218_818.97, daily.balance)
        near(120_000.0, daily.withdrawn, 0.001)
        val min = Calc.savingsAccount(300_000.0, 15.0, 12, 10_000.0, Calc.AccrualMode.MINIMUM)
        near(38_015.20, min.income)
    }

    @Test
    fun savingsAccount_withoutWithdrawals_isMonthlyCompound() {
        val r = Calc.savingsAccount(300_000.0, 15.0, 12, 0.0, Calc.AccrualMode.DAILY)
        near(300_000.0 * (1 + 0.15 / 12).pow(12) - 300_000.0, r.income)
    }

    @Test
    fun goal_roundsUpTo100() {
        // raw = 33 197,10 → 33 200
        assertEquals(33_200.0, Calc.goal(1_000_000.0, 100_000.0, 24, 10.0).monthly, 0.001)
    }

    @Test
    fun mortgage_market_and_family() {
        near(135_905.97, Calc.mortgage(10_000_000.0, 20.0, 20.0, 20, family = false).payment)
        near(76_962.36, Calc.mortgage(10_000_000.0, 20.0, 20.0, 20, family = true).payment)
    }

    @Test
    fun earlyRepayment_reduceTerm() {
        val r = Calc.earlyRepayment(1_000_000.0, 12.0, 120, 10_000.0, Calc.EarlyMode.REDUCE_TERM)
        near(14_347.09, r.basePayment)
        near(427_575.61, r.savedInterest)
        assertEquals(66, r.monthsSaved)
    }

    @Test
    fun earlyRepayment_reducePayment() {
        val r = Calc.earlyRepayment(1_000_000.0, 12.0, 120, 10_000.0, Calc.EarlyMode.REDUCE_PAYMENT)
        near(356_750.26, r.savedInterest)
        near(12_572.96, r.paymentAfterYear)
    }

    @Test
    fun rentVsBuy() {
        val r = Calc.rentVsBuy(8_000_000.0, 20.0, 18.0, 45_000.0, 15)
        near(98_771.94, r.mortgagePayment)
        near(12_741_759.98, r.buyNet)
        near(23_519_164.72, r.rentNet)
        assertFalse(r.buyBetter)
    }
}
