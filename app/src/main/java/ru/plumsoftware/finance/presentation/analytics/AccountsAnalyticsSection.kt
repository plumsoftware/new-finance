package ru.plumsoftware.finance.presentation.analytics

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

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.analytics_accounts_section),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface.copy(alpha = 0.45f),
                letterSpacing = 0.5.sp,
            )

            if (accounts.size > 2) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = if (isExpanded) {
                            stringResource(R.string.analytics_accounts_collapse)
                        } else {
                            stringResource(R.string.analytics_accounts_expand)
                        },
                        fontSize = 14.sp,
                        color = IosBlue,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface),
        ) {
            val visibleAccounts = if (isExpanded || accounts.size <= 2) {
                accounts
            } else {
                accounts.take(2)
            }

            visibleAccounts.forEachIndexed { index, item ->
                AccountAnalyticsRow(
                    item = item,
                    currencyCode = currencyCode,
                )
                if (index < visibleAccounts.lastIndex) {
                    HorizontalDivider(
                        color = colors.onSurface.copy(alpha = 0.07f),
                        modifier = Modifier.padding(start = 60.dp),
                    )
                }
            }

            if (accounts.size > 1) {
                HorizontalDivider(
                    color = colors.onSurface.copy(alpha = 0.07f),
                )
                AccountAnalyticsTotalRow(
                    accounts = accounts,
                    currencyCode = currencyCode,
                )
            }
        }
    }
}

@Composable
fun AccountAnalyticsRow(
    item: AccountAnalytics,
    currencyCode: String,
) {
    val colors = MaterialTheme.colorScheme
    val accountColor = colorFromHexOrDefault(item.account.colorHex, colors.primary)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accountColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(item.account.emoji, fontSize = 18.sp)
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.account.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.analytics_accounts_tx_count,
                        item.transactionCount,
                    ),
                    fontSize = 12.sp,
                    color = colors.onSurface.copy(alpha = 0.4f),
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = MoneyFormat.format(
                        item.balanceMinor,
                        item.account.currencyCode,
                    ),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        item.balanceMinor > 0 -> IosGreen
                        item.balanceMinor < 0 -> IosRed
                        else -> colors.onSurface
                    },
                )
                Text(
                    text = stringResource(item.account.type.localizedNameRes()),
                    fontSize = 11.sp,
                    color = colors.onSurface.copy(alpha = 0.35f),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(IosGreen),
                    )
                    Text(
                        text = stringResource(R.string.analytics_income),
                        fontSize = 11.sp,
                        color = colors.onSurface.copy(alpha = 0.45f),
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = MoneyFormat.format(
                        item.incomeMinor,
                        item.account.currencyCode,
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IosGreen,
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { item.incomeShare },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = IosGreen,
                    trackColor = IosGreen.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(IosRed),
                    )
                    Text(
                        text = stringResource(R.string.analytics_expense),
                        fontSize = 11.sp,
                        color = colors.onSurface.copy(alpha = 0.45f),
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = MoneyFormat.format(
                        item.expenseMinor,
                        item.account.currencyCode,
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = IosRed,
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { item.expenseShare },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = IosRed,
                    trackColor = IosRed.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
fun AccountAnalyticsTotalRow(
    accounts: List<AccountAnalytics>,
    currencyCode: String,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val totalIncome = accounts.sumOf { it.incomeMinor }
    val totalExpense = accounts.sumOf { it.expenseMinor }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.onSurface.copy(alpha = 0.03f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.analytics_accounts_total,
                accounts.size,
            ),
            fontSize = 13.sp,
            color = colors.onSurface.copy(alpha = 0.45f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = MoneyFormat.formatWithSignPrefix(
                    context,
                    totalIncome,
                    currencyCode,
                    isPositive = true,
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = IosGreen,
            )
            Text(
                text = MoneyFormat.formatWithSignPrefix(
                    context,
                    totalExpense,
                    currencyCode,
                    isPositive = false,
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = IosRed,
            )
        }
    }
}
