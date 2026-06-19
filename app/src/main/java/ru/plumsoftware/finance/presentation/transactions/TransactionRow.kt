package ru.plumsoftware.finance.presentation.transactions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.Category
import ru.plumsoftware.finance.domain.model.Transaction
import ru.plumsoftware.finance.domain.model.TransactionType
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionRow(
    transaction: Transaction,
    category: Category?,
    currencyCode: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val isSavings = transaction.type == TransactionType.SAVINGS
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = when {
        isSavings -> colors.onSurfaceVariant
        isIncome -> colors.secondary
        else -> colors.error
    }
    val iconBackgroundColor = when {
        isSavings -> colors.onSurfaceVariant.copy(alpha = 0.12f)
        else -> {
            val categoryColor = category?.colorArgb?.let { Color(it.toInt()) } ?: amountColor
            categoryColor.copy(alpha = 0.15f)
        }
    }
    val formattedDate = formatTransactionDate(transaction.dateMillis)
    val title = when {
        isSavings -> transaction.note ?: stringResource(R.string.type_savings)
        else -> category?.name ?: transaction.note ?: stringResource(R.string.transaction_default)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimens.SpacingM,
                vertical = Dimens.SpacingS,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBackgroundColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSavings) {
                Image(
                    painter = painterResource(R.drawable.pig),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = category?.icon ?: stringResource(R.string.default_bullet),
                    fontSize = 22.sp,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.SpacingM),
        ) {
            Text(
                text = title,
                style = typography.bodyLarge,
                color = if (isSavings) colors.onSurfaceVariant else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formattedDate,
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Text(
            text = MoneyFormat.formatWithSignPrefix(
                context,
                transaction.amountMinor,
                currencyCode,
                isPositive = isIncome,
            ),
            style = typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = amountColor,
        )
    }
}

@Composable
private fun formatTransactionDate(timestamp: Long): String {
    val date = Date(timestamp)
    val dayKey = SimpleDateFormat("yyyyMMdd", Locale.US)
    val today = dayKey.format(Date())
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time.let(dayKey::format)
    val value = dayKey.format(date)
    val time = SimpleDateFormat("HH:mm", Locale("ru")).format(date)
    return when (value) {
        today -> "${stringResource(R.string.today)}, $time"
        yesterday -> "${stringResource(R.string.yesterday)}, $time"
        else -> SimpleDateFormat("d MMM, HH:mm", Locale("ru")).format(date).lowercase()
    }
}
