package ru.plumsoftware.finance.ui.ds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/**
 * Строка списка (§7 `ListRow`): иконка 38–42dp, заголовок 15sp 500, подпись 12sp, значение 15sp 600–700.
 * Min-height 64dp, заголовок до 2 строк (§2 п.6).
 */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    valueColor: Color = FinanceTheme.colors.textPrimary,
    valueCaption: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    titleMaxLines: Int = 2,
) {
    val c = FinanceTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = FinanceType.bodyMedium,
                color = c.textPrimary,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = FinanceType.caption,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (value != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = value,
                    style = FinanceType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = valueColor,
                    maxLines = 1,
                )
                if (valueCaption != null) {
                    Text(text = valueCaption, style = FinanceType.caption, color = c.textSecondary, maxLines = 1)
                }
            }
        }
        trailing?.invoke()
    }
}

/**
 * Строка настроек (§7 `SettingsRow`): плитка 36dp + заголовок 16sp + подпись 13sp + значение / переключатель / шеврон.
 */
@Composable
fun SettingsRow(
    title: String,
    tileIcon: Int,
    tileColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    onSubtitleClick: (() -> Unit)? = null,
    switchChecked: Boolean? = null,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    showChevron: Boolean = switchChecked == null,
    below: (@Composable () -> Unit)? = null,
) {
    val c = FinanceTheme.colors
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsTile(iconRes = tileIcon, color = tileColor)
            Column(Modifier.weight(1f)) {
                Text(text = title, style = FinanceType.body.copy(fontSize = FinanceType.title.fontSize), color = c.textPrimary)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = FinanceType.label.copy(letterSpacing = FinanceType.caption.letterSpacing, fontWeight = FontWeight.Normal),
                        color = if (onSubtitleClick != null) c.primaryTonalText else c.textSecondary,
                        modifier = if (onSubtitleClick != null) Modifier.clickable(onClick = onSubtitleClick) else Modifier,
                    )
                }
            }
            if (value != null) {
                Text(text = value, style = FinanceType.body, color = c.textSecondary, maxLines = 1)
            }
            if (switchChecked != null && onSwitchChange != null) {
                FSwitch(checked = switchChecked, onCheckedChange = onSwitchChange)
            } else if (showChevron) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = c.textDisabled,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        below?.invoke()
    }
}
