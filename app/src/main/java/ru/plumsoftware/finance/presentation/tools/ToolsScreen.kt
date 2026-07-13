package ru.plumsoftware.finance.presentation.tools

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.ui.components.AppCard
import ru.plumsoftware.finance.ui.components.SectionLabel
import ru.plumsoftware.finance.ui.theme.Dimens

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onCreditCalcClick: () -> Unit,
    onDepositCalcClick: () -> Unit,
    onGoalCalcClick: () -> Unit,
    onSavingsAccountCalcClick: () -> Unit,
    onMortgageCalcClick: () -> Unit,
    onEarlyRepayClick: () -> Unit,
    onRentVsBuyClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(top = Dimens.SpacingXxl),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.nav_tools),
                        style = typography.titleLarge.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = Dimens.SpacingM,
                        bottom = Dimens.SpacingXxl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingL),
                ) {
                    item {
                        Column {
                            SectionLabel(text = stringResource(R.string.tools_section_calculators))
                            Column(
                                modifier = Modifier.padding(horizontal = Dimens.SpacingL),
                                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                            ) {
                                ToolCard(
                                    emoji = "💳",
                                    title = stringResource(R.string.tool_credit_title),
                                    subtitle = stringResource(R.string.tool_credit_subtitle),
                                    onClick = onCreditCalcClick,
                                )
                                ToolCard(
                                    emoji = "📈",
                                    title = stringResource(R.string.tool_deposit_title),
                                    subtitle = stringResource(R.string.tool_deposit_subtitle),
                                    onClick = onDepositCalcClick,
                                )
                                ToolCard(
                                    emoji = "🎯",
                                    title = stringResource(R.string.tool_goal_calc_title),
                                    subtitle = stringResource(R.string.tool_goal_calc_subtitle),
                                    onClick = onGoalCalcClick,
                                )
                                ToolCard(
                                    emoji = "🏦",
                                    title = stringResource(R.string.tool_savings_account_title),
                                    subtitle = stringResource(R.string.tool_savings_account_subtitle),
                                    onClick = onSavingsAccountCalcClick,
                                )
                            }
                        }
                    }

                    item {
                        Column {
                            SectionLabel(text = stringResource(R.string.tools_section_advanced))
                            Column(
                                modifier = Modifier.padding(horizontal = Dimens.SpacingL),
                                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
                            ) {
                                ToolCard(
                                    emoji = "🏠",
                                    title = stringResource(R.string.tool_mortgage_title),
                                    subtitle = stringResource(R.string.tool_mortgage_subtitle),
                                    onClick = onMortgageCalcClick,
                                )
                                ToolCard(
                                    emoji = "📉",
                                    title = stringResource(R.string.tool_early_repay_title),
                                    subtitle = stringResource(R.string.tool_early_repay_subtitle),
                                    onClick = onEarlyRepayClick,
                                )
                                ToolCard(
                                    emoji = "⚖️",
                                    title = stringResource(R.string.tool_rent_vs_buy_title),
                                    subtitle = stringResource(R.string.tool_rent_vs_buy_subtitle),
                                    onClick = onRentVsBuyClick,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.SpacingM, vertical = Dimens.SpacingM),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = subtitle,
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    // Параметры ограничений строк удалены для поддержки автоматического переноса текста
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.25f),
                modifier = Modifier.size(Dimens.IconSizeS),
            )
        }
    }
}