package ru.plumsoftware.finance.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionRow(
    transaction: Transaction,
    currencyCode: String,
    categoryIcon: String? = null,
    categoryName: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val dateLabel = SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(Date(transaction.dateMillis))
    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> colors.tertiary
        TransactionType.EXPENSE -> colors.error
        TransactionType.SAVINGS -> colors.tertiary
    }
    val prefix = when (transaction.type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "–"
        TransactionType.SAVINGS -> "+"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${categoryIcon.orEmpty()} ${categoryName ?: transaction.note ?: stringResource(R.string.dash_placeholder)}".trim(),
                    style = typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = dateLabel,
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Text(
                text = "$prefix${MoneyFormat.format(transaction.amountMinor, currencyCode)}",
                style = typography.titleMedium,
                color = amountColor,
            )
        }
    }
}
