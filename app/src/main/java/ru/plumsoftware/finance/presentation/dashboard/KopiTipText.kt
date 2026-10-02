package ru.plumsoftware.finance.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.insights.KopiTip
import ru.plumsoftware.finance.presentation.common.Money

/** Тексты советов Коппи (§8.6). */
@Composable
fun kopiTipText(tip: KopiTip, currency: String): String = when (tip) {
    is KopiTip.DailyExceeded -> stringResource(
        R.string.tip_daily_exceeded,
        Money.formatRounded(tip.over, currency),
        Money.formatRounded(tip.tomorrow, currency),
    )
    is KopiTip.CategoryGrowth -> stringResource(R.string.tip_category_growth, tip.category, tip.percent)
    is KopiTip.ChargeTomorrow -> stringResource(R.string.tip_charge_tomorrow, tip.title, Money.formatRounded(tip.amount, currency))
    is KopiTip.ForecastOverspend -> stringResource(
        R.string.tip_forecast_over,
        Money.formatRounded(tip.over, currency),
        Money.formatRounded(tip.perDay, currency),
    )
    is KopiTip.AssetPaidOff -> stringResource(R.string.tip_asset_paid_off, tip.asset)
    is KopiTip.Neutral -> stringArrayResource(R.array.tip_neutral).let { it[tip.index % it.size] }
}
