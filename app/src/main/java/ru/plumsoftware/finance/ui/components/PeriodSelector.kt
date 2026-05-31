package ru.plumsoftware.finance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.plumsoftware.finance.presentation.common.StatsPeriod
import ru.plumsoftware.finance.ui.components.ios.IosChip
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PeriodSelector(
    selected: StatsPeriod,
    onSelected: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier,
    labels: Map<StatsPeriod, String>,
    customPeriodLabel: String? = null,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
    ) {
        labels.forEach { (period, label) ->
            val displayLabel = if (period == StatsPeriod.CUSTOM && customPeriodLabel != null) {
                customPeriodLabel
            } else {
                label
            }
            IosChip(
                text = displayLabel,
                selected = period == selected,
                onClick = { onSelected(period) },
            )
        }
    }
}
