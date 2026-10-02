package ru.plumsoftware.finance.presentation.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.config.CalcConfigRepository
import ru.plumsoftware.finance.domain.calculators.Calc
import ru.plumsoftware.finance.ui.ds.FSwitch
import ru.plumsoftware.finance.ui.ds.SegmentedLight
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import kotlin.math.roundToInt

@Composable
private fun pct(v: Double): String = "${fmtNumber(v, 1)}%"

@Composable
private fun monthsLabel(n: Int): String = pluralStringResource(R.plurals.pl_months_short, n, n)

@Composable
private fun yearsLabel(n: Int): String = pluralStringResource(R.plurals.pl_years, n, n)

@Composable
private fun termLabel(months: Int): String {
    val y = months / 12
    val m = months % 12
    return when {
        y == 0 -> monthsLabel(m)
        m == 0 -> yearsLabel(y)
        else -> stringResource(R.string.calc_years_months, yearsLabel(y), monthsLabel(m))
    }
}

// region Кредит

@Composable
fun CreditCalculatorScreen(onBack: () -> Unit) {
    val monthsFmt = rememberPluralFormatter(R.plurals.pl_months_short)
    val yearsFmt = rememberPluralFormatter(R.plurals.pl_years)
    var amount by rememberSaveable { mutableDoubleStateOf(1_000_000.0) }
    var rate by rememberSaveable { mutableDoubleStateOf(18.0) }
    var months by rememberSaveable { mutableIntStateOf(36) }
    val r = Calc.credit(amount, rate, months)
    val c = CalcColors
    CalculatorScaffold(
        title = stringResource(R.string.calc_credit),
        emoji = "💳",
        calculatorKey = "credit",
        onBack = onBack,
        result = CalcResult(
            label = stringResource(R.string.calc_monthly_payment),
            value = rub(r.payment),
            parts = listOf(amount.toFloat() to c.Principal, r.overpay.toFloat() to c.Overpay),
            legend = listOf(
                LegendItem(c.Overpay, stringResource(R.string.calc_overpay), rub(r.overpay)),
                LegendItem(c.Principal, stringResource(R.string.calc_full_cost), rub(r.total)),
            ),
        ),
        tip = stringResource(R.string.calc_credit_tip, "${r.overpayPercent.roundToInt()}%"),
        savedDetails = "${rub(amount)} · ${pct(rate)} · ${monthsLabel(months)}",
    ) {
        SliderParam(stringResource(R.string.calc_amount), amount, { amount = it }, 50_000.0, 5_000_000.0, 10_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_rate), rate, { rate = it }, 1.0, 40.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
        SliderParam(stringResource(R.string.calc_term), months.toDouble(), { months = it.toInt() }, 3.0, 84.0, 3.0, { monthsFmt(it.toInt()) })
    }
}


// endregion

// region Вклад

@Composable
fun DepositCalculatorScreen(onBack: () -> Unit) {
    val monthsFmt = rememberPluralFormatter(R.plurals.pl_months_short)
    val yearsFmt = rememberPluralFormatter(R.plurals.pl_years)
    var initial by rememberSaveable { mutableDoubleStateOf(500_000.0) }
    var topUp by rememberSaveable { mutableDoubleStateOf(10_000.0) }
    var rate by rememberSaveable { mutableDoubleStateOf(14.0) }
    var months by rememberSaveable { mutableIntStateOf(12) }
    var capIndex by rememberSaveable { mutableIntStateOf(0) }
    val caps = listOf(Calc.Capitalization.MONTHLY, Calc.Capitalization.QUARTERLY, Calc.Capitalization.YEARLY, Calc.Capitalization.NONE)
    val r = Calc.deposit(initial, topUp, rate, months, caps[capIndex])
    val noCap = Calc.deposit(initial, topUp, rate, months, Calc.Capitalization.NONE)
    CalculatorScaffold(
        title = stringResource(R.string.calc_deposit),
        emoji = "📈",
        calculatorKey = "deposit",
        onBack = onBack,
        result = CalcResult(
            label = stringResource(R.string.calc_deposit_result, monthsLabel(months)),
            value = rub(r.finalAmount),
            parts = listOf(r.invested.toFloat() to CalcColors.Principal, r.interest.toFloat() to CalcColors.Interest),
            legend = listOf(
                LegendItem(CalcColors.Principal, stringResource(R.string.calc_invested), rub(r.invested)),
                LegendItem(CalcColors.Interest, stringResource(R.string.calc_interest), rub(r.interest)),
            ),
        ),
        tip = if (caps[capIndex] != Calc.Capitalization.NONE) {
            stringResource(R.string.calc_deposit_tip_cap, rub(r.finalAmount - noCap.finalAmount))
        } else {
            stringResource(R.string.calc_deposit_tip_nocap)
        },
        savedDetails = "${rub(initial)} + ${rub(topUp)} · ${pct(rate)} · ${monthsLabel(months)}",
    ) {
        Column {
            Text(stringResource(R.string.calc_capitalization), style = FinanceType.bodySmall, color = FinanceTheme.colors.textSecondary)
            SegmentedLight(
                listOf(
                    stringResource(R.string.calc_cap_monthly),
                    stringResource(R.string.calc_cap_quarterly),
                    stringResource(R.string.calc_cap_yearly),
                    stringResource(R.string.calc_cap_none),
                ),
                capIndex,
                { capIndex = it },
            )
        }
        SliderParam(stringResource(R.string.calc_initial), initial, { initial = it }, 0.0, 5_000_000.0, 50_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_topup), topUp, { topUp = it }, 0.0, 150_000.0, 5_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_rate), rate, { rate = it }, 1.0, 25.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
        SliderParam(stringResource(R.string.calc_term), months.toDouble(), { months = it.toInt() }, 3.0, 60.0, 3.0, { monthsFmt(it.toInt()) })
    }
}

// endregion

// region Цель

@Composable
fun GoalCalculatorScreen(onBack: () -> Unit) {
    val monthsFmt = rememberPluralFormatter(R.plurals.pl_months_short)
    val yearsFmt = rememberPluralFormatter(R.plurals.pl_years)
    var target by rememberSaveable { mutableDoubleStateOf(1_000_000.0) }
    var have by rememberSaveable { mutableDoubleStateOf(100_000.0) }
    var months by rememberSaveable { mutableIntStateOf(24) }
    var yieldP by rememberSaveable { mutableDoubleStateOf(10.0) }
    val r = Calc.goal(target, have, months, yieldP)
    CalculatorScaffold(
        title = stringResource(R.string.calc_goal),
        emoji = "🎯",
        calculatorKey = "goal",
        onBack = onBack,
        result = CalcResult(
            label = stringResource(R.string.calc_goal_result),
            value = rub(r.monthly),
            parts = listOf(
                r.have.toFloat() to CalcColors.Have,
                r.contributions.toFloat() to CalcColors.Contributions,
                r.interest.toFloat() to CalcColors.Interest,
            ),
            legend = listOf(
                LegendItem(CalcColors.Have, stringResource(R.string.calc_have_already), rub(r.have)),
                LegendItem(CalcColors.Contributions, stringResource(R.string.calc_contributions), rub(r.contributions)),
                LegendItem(CalcColors.Interest, stringResource(R.string.calc_interest), rub(r.interest)),
            ),
        ),
        tip = stringResource(R.string.calc_goal_tip, rub(r.monthlyWithoutInterest)),
        savedDetails = "${rub(target)} · ${monthsLabel(months)} · ${pct(yieldP)}",
    ) {
        SliderParam(stringResource(R.string.calc_goal_target), target, { target = it }, 100_000.0, 10_000_000.0, 50_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_goal_have), have, { have = it }, 0.0, 5_000_000.0, 10_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_term), months.toDouble(), { months = it.toInt() }, 3.0, 120.0, 3.0, { monthsFmt(it.toInt()) })
        SliderParam(stringResource(R.string.calc_yield), yieldP, { yieldP = it }, 0.0, 20.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
    }
}

// endregion

// region Накопительный счёт

@Composable
fun SavingsAccountCalculatorScreen(onBack: () -> Unit) {
    val monthsFmt = rememberPluralFormatter(R.plurals.pl_months_short)
    val yearsFmt = rememberPluralFormatter(R.plurals.pl_years)
    var amount by rememberSaveable { mutableDoubleStateOf(300_000.0) }
    var rate by rememberSaveable { mutableDoubleStateOf(15.0) }
    var months by rememberSaveable { mutableIntStateOf(12) }
    var wd by rememberSaveable { mutableDoubleStateOf(10_000.0) }
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val mode = if (modeIndex == 0) Calc.AccrualMode.DAILY else Calc.AccrualMode.MINIMUM
    val r = Calc.savingsAccount(amount, rate, months, wd, mode)
    val other = Calc.savingsAccount(amount, rate, months, wd, if (modeIndex == 0) Calc.AccrualMode.MINIMUM else Calc.AccrualMode.DAILY)
    CalculatorScaffold(
        title = stringResource(R.string.calc_savings),
        emoji = "🏦",
        calculatorKey = "savings_account",
        onBack = onBack,
        result = CalcResult(
            label = stringResource(R.string.calc_savings_result, monthsLabel(months)),
            value = rub(r.income),
            parts = listOf(r.balance.toFloat() to CalcColors.Principal, r.withdrawn.toFloat() to CalcColors.Withdrawn),
            legend = listOf(
                LegendItem(CalcColors.Principal, stringResource(R.string.calc_balance), rub(r.balance)),
                LegendItem(CalcColors.Withdrawn, stringResource(R.string.calc_withdrawn), rub(r.withdrawn)),
            ),
        ),
        tip = if (modeIndex == 0) {
            stringResource(R.string.calc_savings_tip_daily, rub(r.income - other.income))
        } else {
            stringResource(R.string.calc_savings_tip_min, rub(other.income - r.income))
        },
        savedDetails = "${rub(amount)} · ${pct(rate)} · ${monthsLabel(months)}",
    ) {
        Column {
            Text(stringResource(R.string.calc_accrual), style = FinanceType.bodySmall, color = FinanceTheme.colors.textSecondary)
            SegmentedLight(
                listOf(stringResource(R.string.calc_accrual_daily), stringResource(R.string.calc_accrual_min)),
                modeIndex,
                { modeIndex = it },
            )
        }
        SliderParam(stringResource(R.string.calc_amount), amount, { amount = it }, 10_000.0, 3_000_000.0, 10_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_rate), rate, { rate = it }, 1.0, 25.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
        SliderParam(stringResource(R.string.calc_term), months.toDouble(), { months = it.toInt() }, 1.0, 36.0, 1.0, { monthsFmt(it.toInt()) })
        SliderParam(stringResource(R.string.calc_withdrawals), wd, { wd = it }, 0.0, 100_000.0, 1_000.0, ::rub)
    }
}

// endregion

// region Ипотека

@Composable
fun MortgageCalculatorScreen(onBack: () -> Unit, config: CalcConfigRepository = koinInject()) {
    val monthsFmt = rememberPluralFormatter(R.plurals.pl_months_short)
    val yearsFmt = rememberPluralFormatter(R.plurals.pl_years)
    val params by config.params.collectAsStateWithLifecycle()
    var price by rememberSaveable { mutableDoubleStateOf(8_000_000.0) }
    var down by rememberSaveable { mutableDoubleStateOf(20.0) }
    var rate by rememberSaveable { mutableDoubleStateOf(20.0) }
    var years by rememberSaveable { mutableIntStateOf(20) }
    var family by rememberSaveable { mutableIntStateOf(0) }
    val r = Calc.mortgage(price, down, rate, years, family == 1, params.familyMortgageRatePercent, params.familyMortgageLimitRub)
    CalculatorScaffold(
        title = stringResource(R.string.calc_mortgage),
        emoji = "🏠",
        calculatorKey = "mortgage",
        onBack = onBack,
        result = CalcResult(
            label = stringResource(R.string.calc_monthly_payment),
            value = rub(r.payment),
            secondLine = stringResource(R.string.calc_loan_amount, rub(r.loan)),
            parts = listOf(r.loan.toFloat() to CalcColors.Principal, r.overpay.toFloat() to CalcColors.Overpay),
            legend = listOf(
                LegendItem(CalcColors.Principal, stringResource(R.string.calc_loan), rub(r.loan)),
                LegendItem(CalcColors.Overpay, stringResource(R.string.calc_overpay), rub(r.overpay)),
            ),
        ),
        tip = stringResource(R.string.calc_mortgage_tip, rub(r.comfortableIncome)),
        savedDetails = "${rub(price)} · ${fmtNumber(down)}% · ${pct(rate)} · ${yearsLabel(years)}",
    ) {
        SegmentedLight(
            listOf(
                stringResource(R.string.calc_mortgage_market),
                stringResource(R.string.calc_mortgage_family, fmtNumber(params.familyMortgageRatePercent, 1)),
            ),
            family,
            { family = it },
        )
        SliderParam(stringResource(R.string.calc_price), price, { price = it }, 1_000_000.0, 30_000_000.0, 100_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_down), down, { down = it }, 10.0, 90.0, 1.0, { "${fmtNumber(it)}% · ${rub(price * it / 100)}" })
        SliderParam(stringResource(R.string.calc_rate), rate, { rate = it }, 1.0, 30.0, 0.1, { "${fmtNumber(it, 1)}%" }, 1)
        SliderParam(stringResource(R.string.calc_term_years), years.toDouble(), { years = it.toInt() }, 1.0, 30.0, 1.0, { yearsFmt(it.toInt()) })
    }
}


// endregion

// region Досрочное погашение

@Composable
fun EarlyRepaymentCalculatorScreen(onBack: () -> Unit) {
    val monthsFmt = rememberPluralFormatter(R.plurals.pl_months_short)
    val yearsFmt = rememberPluralFormatter(R.plurals.pl_years)
    var balance by rememberSaveable { mutableDoubleStateOf(3_000_000.0) }
    var rate by rememberSaveable { mutableDoubleStateOf(16.0) }
    var years by rememberSaveable { mutableIntStateOf(15) }
    var extra by rememberSaveable { mutableDoubleStateOf(20_000.0) }
    var modeIndex by rememberSaveable { mutableIntStateOf(0) }
    val mode = if (modeIndex == 0) Calc.EarlyMode.REDUCE_TERM else Calc.EarlyMode.REDUCE_PAYMENT
    val r = Calc.earlyRepayment(balance, rate, years * 12, extra, mode)
    CalculatorScaffold(
        title = stringResource(R.string.calc_early),
        emoji = "📉",
        calculatorKey = "early_repay",
        onBack = onBack,
        result = CalcResult(
            label = stringResource(R.string.calc_early_result),
            value = rub(r.savedInterest),
            secondLine = if (mode == Calc.EarlyMode.REDUCE_TERM) {
                stringResource(R.string.calc_early_term_cut, termLabel(r.monthsSaved))
            } else {
                stringResource(R.string.calc_early_payment_year, rub(r.paymentAfterYear))
            },
            parts = listOf(r.newInterest.toFloat() to CalcColors.Overpay, r.savedInterest.toFloat() to CalcColors.Interest),
            legend = listOf(
                LegendItem(CalcColors.Overpay, stringResource(R.string.calc_interest_left), rub(r.newInterest)),
                if (mode == Calc.EarlyMode.REDUCE_TERM) {
                    LegendItem(CalcColors.Interest, stringResource(R.string.calc_term_cut_short), termLabel(r.monthsSaved))
                } else {
                    LegendItem(CalcColors.Interest, stringResource(R.string.calc_payment_after_year), rub(r.paymentAfterYear))
                },
            ),
        ),
        tip = stringResource(R.string.calc_early_tip, rub(r.basePayment), rub(r.baseInterest)),
        savedDetails = "${rub(balance)} · ${pct(rate)} · ${yearsLabel(years)} · +${rub(extra)}",
    ) {
        SegmentedLight(
            listOf(stringResource(R.string.calc_early_mode_term), stringResource(R.string.calc_early_mode_payment)),
            modeIndex,
            { modeIndex = it },
        )
        SliderParam(stringResource(R.string.calc_debt), balance, { balance = it }, 100_000.0, 20_000_000.0, 50_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_rate), rate, { rate = it }, 1.0, 30.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
        SliderParam(stringResource(R.string.calc_years_left), years.toDouble(), { years = it.toInt() }, 1.0, 30.0, 1.0, { yearsFmt(it.toInt()) })
        SliderParam(stringResource(R.string.calc_extra), extra, { extra = it }, 0.0, 200_000.0, 1_000.0, ::rub)
    }
}

// endregion

// region Аренда или ипотека

@Composable
fun RentVsBuyScreen(onBack: () -> Unit) {
    val monthsFmt = rememberPluralFormatter(R.plurals.pl_months_short)
    val yearsFmt = rememberPluralFormatter(R.plurals.pl_years)
    var price by rememberSaveable { mutableDoubleStateOf(8_000_000.0) }
    var down by rememberSaveable { mutableDoubleStateOf(20.0) }
    var rate by rememberSaveable { mutableDoubleStateOf(18.0) }
    var rent by rememberSaveable { mutableDoubleStateOf(45_000.0) }
    var horizon by rememberSaveable { mutableIntStateOf(15) }
    var advanced by rememberSaveable { mutableStateOf(false) }
    var investRate by rememberSaveable { mutableDoubleStateOf(10.0) }
    var rentGrowth by rememberSaveable { mutableDoubleStateOf(5.0) }
    var homeGrowth by rememberSaveable { mutableDoubleStateOf(5.0) }
    var mortgageYears by rememberSaveable { mutableIntStateOf(20) }
    val a = Calc.RentVsBuyAssumptions(mortgageYears, investRate, rentGrowth, homeGrowth)
    val r = Calc.rentVsBuy(price, down, rate, rent, horizon, a)
    val c = FinanceTheme.colors
    CalculatorScaffold(
        title = stringResource(R.string.calc_rent_vs_buy),
        emoji = "⚖️",
        calculatorKey = "rent_vs_buy",
        onBack = onBack,
        result = CalcResult(
            label = stringResource(
                if (r.buyBetter) R.string.calc_rvb_buy_better else R.string.calc_rvb_rent_better,
                yearsLabel(horizon),
            ),
            value = rub(r.advantage),
            secondLine = stringResource(R.string.calc_rvb_payment, rub(r.mortgagePayment)),
            parts = listOf(r.buyNet.coerceAtLeast(0.0).toFloat() to CalcColors.Principal, r.rentNet.coerceAtLeast(0.0).toFloat() to CalcColors.Withdrawn),
            legend = listOf(
                LegendItem(CalcColors.Principal, stringResource(R.string.calc_rvb_buy), rub(r.buyNet)),
                LegendItem(CalcColors.Withdrawn, stringResource(R.string.calc_rvb_rent), rub(r.rentNet)),
            ),
        ),
        tip = stringResource(
            R.string.calc_rvb_tip,
            mortgageYears,
            fmtNumber(investRate, 1),
            fmtNumber(rentGrowth, 1),
            fmtNumber(homeGrowth, 1),
        ),
        savedDetails = "${rub(price)} · ${fmtNumber(down)}% · ${pct(rate)} · ${rub(rent)} · ${yearsLabel(horizon)}",
    ) {
        SliderParam(stringResource(R.string.calc_price), price, { price = it }, 1_000_000.0, 30_000_000.0, 100_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_down), down, { down = it }, 10.0, 90.0, 1.0, { "${fmtNumber(it)}%" })
        SliderParam(stringResource(R.string.calc_mortgage_rate), rate, { rate = it }, 1.0, 30.0, 0.1, { "${fmtNumber(it, 1)}%" }, 1)
        SliderParam(stringResource(R.string.calc_rent), rent, { rent = it }, 10_000.0, 200_000.0, 1_000.0, ::rub)
        SliderParam(stringResource(R.string.calc_horizon), horizon.toDouble(), { horizon = it.toInt() }, 10.0, 30.0, 1.0, { yearsFmt(it.toInt()) })
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.calc_advanced), style = FinanceType.title.copy(fontWeight = FontWeight.SemiBold), color = c.textPrimary, modifier = Modifier.weight(1f))
            FSwitch(advanced, { advanced = it })
        }
        if (advanced) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                SliderParam(stringResource(R.string.calc_rvb_mortgage_term), mortgageYears.toDouble(), { mortgageYears = it.toInt() }, 5.0, 30.0, 1.0, { yearsFmt(it.toInt()) })
                SliderParam(stringResource(R.string.calc_rvb_invest), investRate, { investRate = it }, 0.0, 25.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
                SliderParam(stringResource(R.string.calc_rvb_rent_growth), rentGrowth, { rentGrowth = it }, 0.0, 20.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
                SliderParam(stringResource(R.string.calc_rvb_home_growth), homeGrowth, { homeGrowth = it }, 0.0, 20.0, 0.5, { "${fmtNumber(it, 1)}%" }, 1)
            }
        }
    }
}

// endregion
