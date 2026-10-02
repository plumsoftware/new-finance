package ru.plumsoftware.finance.domain.calculators

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Формулы калькуляторов (ТЗ §8.5). Все суммы — в рублях (Double), ставки — в процентах годовых.
 */
object Calc {

    /** Аннуитетный платёж: `P × i / (1 − (1 + i)^−n)`, `i = r / 1200`; при `i = 0` → `P / n`. */
    fun ann(principal: Double, ratePercent: Double, months: Int): Double {
        if (months <= 0 || principal <= 0) return 0.0
        val i = ratePercent / 1200.0
        return if (i == 0.0) principal / months else principal * i / (1 - (1 + i).pow(-months))
    }

    // region Кредит

    data class CreditResult(val payment: Double, val overpay: Double, val total: Double, val overpayPercent: Double)

    fun credit(amount: Double, ratePercent: Double, months: Int): CreditResult {
        val p = ann(amount, ratePercent, months)
        val total = p * months
        val over = total - amount
        return CreditResult(p, over, total, if (amount > 0) over / amount * 100 else 0.0)
    }

    // endregion

    // region Вклад

    enum class Capitalization(val periodMonths: Int) { MONTHLY(1), QUARTERLY(3), YEARLY(12), NONE(0) }

    data class DepositResult(val finalAmount: Double, val invested: Double, val interest: Double)

    /**
     * Помесячно: проценты `bal × r / 12`; при капитализации прибавляются к телу в конце периода,
     * иначе копятся отдельно; пополнение — в конце месяца.
     */
    fun deposit(
        initial: Double,
        monthlyTopUp: Double,
        ratePercent: Double,
        months: Int,
        cap: Capitalization,
    ): DepositResult {
        var body = initial
        var pending = 0.0
        var separate = 0.0
        for (m in 1..months) {
            val interest = body * ratePercent / 100.0 / 12.0
            if (cap == Capitalization.NONE) {
                separate += interest
            } else {
                pending += interest
                if (m % cap.periodMonths == 0) {
                    body += pending
                    pending = 0.0
                }
            }
            body += monthlyTopUp
        }
        val invested = initial + monthlyTopUp * months
        val final = body + pending + separate
        return DepositResult(final, invested, final - invested)
    }

    // endregion

    // region Цель

    data class GoalResult(
        val monthly: Double,
        val have: Double,
        val contributions: Double,
        val interest: Double,
        val monthlyWithoutInterest: Double,
    )

    /** `g = (1+i)^n`; `need = (target − have × g) × i / (g − 1)`; округление вверх до 100. */
    fun goal(target: Double, have: Double, months: Int, yieldPercent: Double): GoalResult {
        val n = max(months, 1)
        val i = yieldPercent / 1200.0
        val g = (1 + i).pow(n)
        val raw = if (i == 0.0) (target - have) / n else (target - have * g) * i / (g - 1)
        val need = if (raw <= 0) 0.0 else ceil(raw / 100.0) * 100.0
        val contributions = need * n
        // Фактические проценты = итог накоплений − вложенное.
        val fv = if (i == 0.0) have + contributions else have * g + need * (g - 1) / i
        val interest = max(0.0, fv - have - contributions)
        val withoutInterest = max(0.0, (target - have) / n)
        return GoalResult(need, have, contributions, interest, withoutInterest)
    }

    // endregion

    // region Накопительный счёт

    enum class AccrualMode { DAILY, MINIMUM }

    data class SavingsResult(val income: Double, val balance: Double, val withdrawn: Double)

    /** База месяца: ежедневный — `bal − wd/2`, минимальный — `bal − wd`; проценты капитализируются. */
    fun savingsAccount(
        amount: Double,
        ratePercent: Double,
        months: Int,
        monthlyWithdrawal: Double,
        mode: AccrualMode,
    ): SavingsResult {
        var bal = amount
        var income = 0.0
        var withdrawn = 0.0
        repeat(months) {
            val wd = min(monthlyWithdrawal, max(bal, 0.0))
            val base = max(0.0, if (mode == AccrualMode.DAILY) bal - wd / 2 else bal - wd)
            val interest = base * ratePercent / 100.0 / 12.0
            bal = bal - wd + interest
            income += interest
            withdrawn += wd
        }
        return SavingsResult(income, bal, withdrawn)
    }

    // endregion

    // region Ипотека

    data class MortgageResult(val loan: Double, val payment: Double, val overpay: Double, val comfortableIncome: Double)

    fun mortgage(
        price: Double,
        downPercent: Double,
        ratePercent: Double,
        years: Int,
        family: Boolean,
        familyRatePercent: Double = 6.0,
        familyLimit: Double = 6_000_000.0,
    ): MortgageResult {
        val n = years * 12
        val loan = price * (1 - downPercent / 100.0)
        val payment = if (family) {
            val subsidized = min(loan, familyLimit)
            ann(subsidized, familyRatePercent, n) + ann(loan - subsidized, ratePercent, n)
        } else {
            ann(loan, ratePercent, n)
        }
        return MortgageResult(loan, payment, payment * n - loan, payment / 0.5)
    }

    // endregion

    // region Досрочное погашение

    enum class EarlyMode { REDUCE_TERM, REDUCE_PAYMENT }

    data class EarlyResult(
        val savedInterest: Double,
        val baseInterest: Double,
        val newInterest: Double,
        val basePayment: Double,
        val monthsSaved: Int,
        val newMonths: Int,
        /** Платёж через год (для режима «уменьшать платёж»). */
        val paymentAfterYear: Double,
    )

    fun earlyRepayment(balance: Double, ratePercent: Double, months: Int, extra: Double, mode: EarlyMode): EarlyResult {
        val i = ratePercent / 1200.0
        val base = ann(balance, ratePercent, months)
        val baseInt = base * months - balance
        var bal = balance
        var totalInt = 0.0
        var k = 0
        var paymentAfterYear = base
        when (mode) {
            EarlyMode.REDUCE_TERM -> {
                while (bal > 0.005 && k < months) {
                    val interest = bal * i
                    totalInt += interest
                    val principal = min(base + extra - interest, bal)
                    bal -= principal
                    k++
                }
            }
            EarlyMode.REDUCE_PAYMENT -> {
                while (bal > 0.005 && k < months) {
                    val p = ann(bal, ratePercent, months - k)
                    if (k == 12) paymentAfterYear = p
                    val interest = bal * i
                    totalInt += interest
                    val principal = min(p - interest + extra, bal)
                    bal -= principal
                    k++
                }
                if (k <= 12) paymentAfterYear = 0.0
            }
        }
        return EarlyResult(
            savedInterest = max(0.0, baseInt - totalInt),
            baseInterest = baseInt,
            newInterest = totalInt,
            basePayment = base,
            monthsSaved = max(0, months - k),
            newMonths = k,
            paymentAfterYear = paymentAfterYear,
        )
    }

    // endregion

    // region Аренда или ипотека

    data class RentVsBuyAssumptions(
        val mortgageYears: Int = 20,
        val investRatePercent: Double = 10.0,
        val rentGrowthPercent: Double = 5.0,
        val homeGrowthPercent: Double = 5.0,
    )

    data class RentVsBuyResult(
        val buyNet: Double,
        val rentNet: Double,
        val mortgagePayment: Double,
        val buyBetter: Boolean,
        val advantage: Double,
    )

    /**
     * Каждый месяц бюджет = max(платёж, аренда); разница инвестируется. Аренда и жильё дорожают раз в год.
     * Покупка: стоимость жилья − остаток долга + инвестиции. Аренда: первый взнос и разницы, инвестированные.
     */
    fun rentVsBuy(
        price: Double,
        downPercent: Double,
        mortgageRatePercent: Double,
        monthlyRent: Double,
        horizonYears: Int,
        a: RentVsBuyAssumptions = RentVsBuyAssumptions(),
    ): RentVsBuyResult {
        val down = price * downPercent / 100.0
        val loan = price - down
        val n = a.mortgageYears * 12
        val payment = ann(loan, mortgageRatePercent, n)
        val iMortgage = mortgageRatePercent / 1200.0
        val iInvest = a.investRatePercent / 1200.0
        var debt = loan
        var rent = monthlyRent
        var buyInvest = 0.0
        var rentInvest = down
        val total = horizonYears * 12
        for (m in 1..total) {
            if (m > 1 && (m - 1) % 12 == 0) rent *= 1 + a.rentGrowthPercent / 100.0
            val pay = if (m <= n && debt > 0) min(payment, debt * (1 + iMortgage)) else 0.0
            if (debt > 0) debt = max(0.0, debt * (1 + iMortgage) - pay)
            val budget = max(pay, rent)
            buyInvest = buyInvest * (1 + iInvest) + (budget - pay)
            rentInvest = rentInvest * (1 + iInvest) + (budget - rent)
        }
        val homeValue = price * (1 + a.homeGrowthPercent / 100.0).pow(horizonYears)
        val buyNet = homeValue - debt + buyInvest
        return RentVsBuyResult(
            buyNet = buyNet,
            rentNet = rentInvest,
            mortgagePayment = payment,
            buyBetter = buyNet >= rentInvest,
            advantage = kotlin.math.abs(buyNet - rentInvest),
        )
    }

    // endregion
}
