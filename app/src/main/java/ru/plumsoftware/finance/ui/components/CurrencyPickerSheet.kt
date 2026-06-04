package ru.plumsoftware.finance.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.util.SupportedCurrencies
import ru.plumsoftware.finance.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerSheet(
    selectedCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
    ) {
        Column(modifier = Modifier.padding(bottom = Dimens.bottomSheetBottomPadding)) {
            Text(
                text = stringResource(R.string.currency_picker_title),
                style = typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Dimens.spacingSection, vertical = Dimens.spacingList),
            )
            Text(
                text = stringResource(R.string.currency_popular_section),
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.spacingSection),
            )
            LazyColumn {
                items(SupportedCurrencies.all, key = { it.code }) { currency ->
                    val selected = currency.code == selectedCode
                    Text(
                        text = "${currency.code} · ${currency.name}",
                        style = typography.bodyLarge,
                        color = if (selected) colors.primary else colors.onSurface,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(currency.code)
                                onDismiss()
                            }
                            .padding(horizontal = Dimens.spacingSection, vertical = Dimens.spacingRow),
                    )
                    HorizontalDivider(
                        color = colors.outline.copy(alpha = 0.3f),
                        modifier = Modifier.padding(start = Dimens.spacingSection),
                    )
                }
            }
        }
    }
}
