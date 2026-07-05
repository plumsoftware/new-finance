package ru.plumsoftware.finance.presentation.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.AccountAnalytics
import ru.plumsoftware.finance.domain.model.localizedNameRes
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.presentation.goals.colorFromHexOrDefault
import ru.plumsoftware.finance.ui.theme.Dimens
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed

@Composable
fun AccountsAnalyticsSection(
    accounts: List<AccountAnalytics>,
    currencyCode: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (accounts.isEmpty()) return

    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(modifier = modifier) {

        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.analytics_accounts_section),
                style = typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface.copy(alpha = 0.45f),
                letterSpacing = 0.8.sp,
            )
            if (accounts.size > 2) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = if (isExpanded)
                            stringResource(R.string.analytics_accounts_collapse)
                        else
                            stringResource(R.string.analytics_accounts_expand),
                        fontSize = 13.sp,
                        color = IosBlue,
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingS))

        // Cards container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingL)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface),
        ) {
            val visibleAccounts = if (isExpanded || accounts.size <= 2) accounts
            else accounts.take(2)

            visibleAccounts.forEachIndexed { index, item ->
                AccountAnalyticsRow(item = item, currencyCode = currencyCode)
                if (index < visibleAccounts.lastIndex) {
                    HorizontalDivider(
                        color = colors.onSurface.copy(alpha = 0.06f),
                        modifier = Modifier.padding(start = 60.dp),
                    )
                }
            }

            // Total footer — only if multiple accounts
            if (accounts.size > 1) {
                HorizontalDivider(color = colors.onSurface.copy(alpha = 0.06f))
                AccountAnalyticsTotalRow(accounts = accounts, currencyCode = currencyCode)
            }
        }
    }
}

// ── Single account row ────────────────────────────────────────────────────────

@Composable
fun AccountAnalyticsRow(
    item: AccountAnalytics,
    currencyCode: String,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val accountColor = colorFromHexOrDefault(item.account.colorHex, colors.primary)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        // Account identity row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Emoji avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accountColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.account.emoji, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.account.name,
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.analytics_accounts_tx_count, item.transactionCount),
                    style = typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.4f),
                )
            }

            // Balance + account type
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MoneyFormat.format(item.balanceMinor, item.account.currencyCode),
                    style = typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        item.balanceMinor > 0 -> IosGreen
                        item.balanceMinor < 0 -> IosRed
                        else -> colors.onSurface
                    },
                )
                Text(
                    text = stringResource(item.account.type.localizedNameRes()),
                    style = typography.labelSmall,
                    color = colors.onSurface.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Income / Expense mini bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccountStatBar(
                dotColor = IosGreen,
                label = stringResource(R.string.analytics_income),
                value = MoneyFormat.format(item.incomeMinor, item.account.currencyCode),
                valueColor = IosGreen,
                progress = item.incomeShare,
                progressColor = IosGreen,
                modifier = Modifier.weight(1f),
            )
            AccountStatBar(
                dotColor = IosRed,
                label = stringResource(R.string.analytics_expense),
                value = MoneyFormat.format(item.expenseMinor, item.account.currencyCode),
                valueColor = IosRed,
                progress = item.expenseShare,
                progressColor = IosRed,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// ── AccountStatBar ─────────────────────────────────────────────────────────────
// A cleaner sub-component for the income/expense progress bars

@Composable
private fun AccountStatBar(
    dotColor: Color,
    label: String,
    value: String,
    valueColor: Color,
    progress: Float,
    progressColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Text(
                text = label,
                style = typography.labelSmall,
                color = colors.onSurface.copy(alpha = 0.45f),
                fontSize = 11.sp,
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.10f),
            strokeCap = StrokeCap.Round,
        )
    }
}

// ── Total footer row ──────────────────────────────────────────────────────────

@Composable
fun AccountAnalyticsTotalRow(
    accounts: List<AccountAnalytics>,
    currencyCode: String,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val totalIncome = accounts.sumOf { it.incomeMinor }
    val totalExpense = accounts.sumOf { it.expenseMinor }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.onSurface.copy(alpha = 0.025f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.analytics_accounts_total, accounts.size),
            style = typography.labelSmall,
            color = colors.onSurface.copy(alpha = 0.45f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = MoneyFormat.formatWithSignPrefix(context, totalIncome, currencyCode, isPositive = true),
                style = typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = IosGreen,
            )
            Text(
                text = MoneyFormat.formatWithSignPrefix(context, totalExpense, currencyCode, isPositive = false),
                style = typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = IosRed,
            )
        }
    }
}
