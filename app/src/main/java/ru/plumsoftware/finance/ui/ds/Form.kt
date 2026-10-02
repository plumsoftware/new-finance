package ru.plumsoftware.finance.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Однострочное поле ввода внутри карточки формы. */
@Composable
fun FormTextField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    val c = FinanceTheme.colors
    Box(modifier.fillMaxWidth().heightIn(min = 40.dp), contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) Text(placeholder, style = FinanceType.body, color = c.textSecondary)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = FinanceType.body.copy(color = c.textPrimary),
            cursorBrush = SolidColor(c.primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Строка «подпись — значение» с переходом к выбору (сумма, дата, валюта). */
@Composable
fun FormValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    valueColor: Color = FinanceTheme.colors.primary,
    enabled: Boolean = true,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = FinanceType.body, color = FinanceTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        Text(value, style = FinanceType.title, color = valueColor)
    }
}

/** Строка с переключателем и необязательной подписью. */
@Composable
fun FormSwitchRow(label: String, checked: Boolean, hint: String? = null, onChange: (Boolean) -> Unit) {
    val c = FinanceTheme.colors
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(label, style = FinanceType.body, color = c.textPrimary)
            if (hint != null) Text(hint, style = FinanceType.caption, color = c.textSecondary)
        }
        HSpace(12.dp)
        FSwitch(checked, onChange)
    }
}

/** Сетка выбора эмодзи (по 6 в ряд). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmojiPicker(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val c = FinanceTheme.colors
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 6,
    ) {
        options.forEach { emoji ->
            val isSelected = emoji == selected
            Box(
                Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) c.primaryTonalBg else c.bg)
                    .then(if (isSelected) Modifier.border(1.5.dp, c.primary, RoundedCornerShape(14.dp)) else Modifier)
                    .clickable(role = Role.RadioButton) { onSelect(emoji) }
                    .semantics { this.selected = isSelected },
                contentAlignment = Alignment.Center,
            ) { Text(emoji, fontSize = 22.sp) }
        }
    }
}

/** Нижняя закреплённая панель с основной кнопкой. */
@Composable
fun BottomActionBar(text: String, onClick: () -> Unit, enabled: Boolean) {
    androidx.compose.foundation.layout.Column(
        Modifier
            .fillMaxWidth()
            .background(FinanceTheme.colors.surface)
            .navigationBarsPadding()
            .padding(16.dp),
    ) {
        ButtonPrimary(text = text, onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth())
    }
}

