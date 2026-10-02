package ru.plumsoftware.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.util.CurrencyInfo
import ru.plumsoftware.finance.domain.util.SupportedCurrencies
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.FBottomSheet
import ru.plumsoftware.finance.ui.ds.FitText
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/** Выбор валюты: нижняя панель §6.5/§7 с поиском и секциями «Популярные» / «Все валюты». */
@Composable
fun CurrencyPickerSheet(
    selectedCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = FinanceTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    val names = SupportedCurrencies.all.associate { it.code to stringResource(it.nameRes) }
    fun matches(info: CurrencyInfo): Boolean {
        val q = query.trim()
        return q.isEmpty() || info.code.contains(q, ignoreCase = true) || names[info.code].orEmpty().contains(q, ignoreCase = true)
    }
    val popular = SupportedCurrencies.popular.filter(::matches)
    val others = SupportedCurrencies.all.filter { it !in SupportedCurrencies.popular }.filter(::matches)

    FBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Text(
                stringResource(R.string.currency_picker_title),
                style = FinanceType.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = c.textPrimary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            VSpace(12.dp)
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(c.searchField, RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_search), null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                HSpace(10.dp)
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) Text(stringResource(R.string.currency_search_hint), style = FinanceType.body, color = c.textSecondary)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it.take(30) },
                        singleLine = true,
                        textStyle = FinanceType.body.copy(color = c.textPrimary),
                        cursorBrush = SolidColor(c.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            VSpace(8.dp)
            LazyColumn(
                Modifier.heightIn(max = 520.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                if (popular.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.currency_popular_section)) }
                    items(popular, key = { "p" + it.code }) { info ->
                        CurrencyRow(info, names[info.code].orEmpty(), info.code == selectedCode) {
                            onSelect(info.code)
                            onDismiss()
                        }
                    }
                }
                if (others.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.currency_all_section)) }
                    items(others, key = { "o" + it.code }) { info ->
                        CurrencyRow(info, names[info.code].orEmpty(), info.code == selectedCode) {
                            onSelect(info.code)
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(info: CurrencyInfo, name: String, selected: Boolean, onClick: () -> Unit) {
    val c = FinanceTheme.colors
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.RadioButton, onClick = onClick)
                .semantics { this.selected = selected }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(if (selected) c.primaryTonalBg else c.bg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                FitText(
                    info.symbol,
                    style = FinanceType.title.copy(fontWeight = FontWeight.Bold),
                    color = if (selected) c.primaryTonalText else c.textPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            HSpace(12.dp)
            Column(Modifier.weight(1f)) {
                Text(name, style = FinanceType.bodyMedium, color = c.textPrimary)
                Text(info.code, style = FinanceType.caption, color = c.textSecondary)
            }
            if (selected) {
                Icon(painterResource(R.drawable.ic_check), null, tint = c.primary, modifier = Modifier.size(22.dp))
            }
        }
        CardDivider(Modifier.padding(start = 52.dp))
    }
}
