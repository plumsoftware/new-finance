package ru.plumsoftware.finance.ui.components.ios

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.LightTextSecondary

@Composable
fun IosSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = androidx.compose.ui.graphics.Color.White,
            checkedTrackColor = IosGreen,
            uncheckedThumbColor = androidx.compose.ui.graphics.Color.White,
            uncheckedTrackColor = LightTextSecondary.copy(alpha = 0.35f),
            uncheckedBorderColor = LightTextSecondary.copy(alpha = 0.35f),
            checkedBorderColor = IosGreen,
        ),
    )
}
