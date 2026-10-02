package ru.plumsoftware.finance.presentation.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import ru.plumsoftware.finance.AppConfig
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.config.SavedCalculationsRepository
import ru.plumsoftware.finance.ui.ads.NativeAdContainer
import ru.plumsoftware.finance.ui.ads.NativeAdSession
import ru.plumsoftware.finance.ui.ds.CardDivider
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.ListRow
import ru.plumsoftware.finance.ui.ds.RootBottomInset
import ru.plumsoftware.finance.ui.ds.SectionHeader
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

private data class CalcEntry(val emoji: String, val bg: Color, val titleRes: Int, val descRes: Int, val onClick: () -> Unit)

/** Вкладка «Расчёты» (§6.7). */
@Composable
fun ToolsScreen(
    onCreditCalcClick: () -> Unit,
    onDepositCalcClick: () -> Unit,
    onGoalCalcClick: () -> Unit,
    onSavingsAccountCalcClick: () -> Unit,
    onMortgageCalcClick: () -> Unit,
    onEarlyRepayClick: () -> Unit,
    onRentVsBuyClick: () -> Unit,
    onSavedClick: () -> Unit = {},
    saved: SavedCalculationsRepository = koinInject(),
) {
    val c = FinanceTheme.colors
    val savedItems by saved.items.collectAsStateWithLifecycle()
    val popular = listOf(
        CalcEntry("💳", Color(0xFFFFF4D6), R.string.calc_credit, R.string.calc_credit_desc, onCreditCalcClick),
        CalcEntry("📈", Color(0xFFE3F7E8), R.string.calc_deposit, R.string.calc_deposit_desc, onDepositCalcClick),
        CalcEntry("🎯", Color(0xFFFFE5E3), R.string.calc_goal, R.string.calc_goal_desc, onGoalCalcClick),
        CalcEntry("🏦", Color(0xFFE3EDFF), R.string.calc_savings, R.string.calc_savings_desc, onSavingsAccountCalcClick),
    )
    val advanced = listOf(
        CalcEntry("🏠", Color(0xFFEDE7FF), R.string.calc_mortgage, R.string.calc_mortgage_desc, onMortgageCalcClick),
        CalcEntry("📉", Color(0xFFE3F4FF), R.string.calc_early, R.string.calc_early_desc, onEarlyRepayClick),
        CalcEntry("⚖️", Color(0xFFF2F3F7), R.string.calc_rent_vs_buy, R.string.calc_rent_vs_buy_desc, onRentVsBuyClick),
    )
    LazyColumn(
        Modifier.fillMaxSize().background(c.bg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = RootBottomInset),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.statusBarsPadding().padding(top = 12.dp)) {
                Text(stringResource(R.string.nav_tools), style = FinanceType.headline, color = c.textPrimary)
                Text(stringResource(R.string.tools_subtitle), style = FinanceType.bodySmall, color = c.textSecondary)
            }
        }
        item { SectionHeader(stringResource(R.string.tools_popular)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                popular.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { e ->
                            FCard(modifier = Modifier.weight(1f).heightIn(min = 140.dp), onClick = e.onClick) {
                                Tile(e.emoji, e.bg)
                                VSpace(12.dp)
                                Text(stringResource(e.titleRes), style = FinanceType.titleBold, color = c.textPrimary)
                                Text(stringResource(e.descRes), style = FinanceType.caption, color = c.textSecondary)
                            }
                        }
                    }
                }
            }
        }
        if (AppConfig.nativeTools.isNotBlank() && !NativeAdSession.isDismissed(AppConfig.nativeTools)) {
            item { FCard(padding = PaddingValues(0.dp)) { NativeAdContainer(adUnitId = AppConfig.nativeTools) } }
        }
        item { SectionHeader(stringResource(R.string.tools_advanced)) }
        item {
            FCard(padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                advanced.forEachIndexed { i, e ->
                    if (i > 0) CardDivider()
                    ListRow(
                        title = stringResource(e.titleRes),
                        subtitle = stringResource(e.descRes),
                        leading = { Tile(e.emoji, e.bg) },
                        trailing = { Chevron() },
                        onClick = e.onClick,
                    )
                }
            }
        }
        item {
            FCard(padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                ListRow(
                    title = stringResource(R.string.tools_saved),
                    subtitle = if (savedItems.isEmpty()) stringResource(R.string.tools_saved_empty_sub) else null,
                    value = if (savedItems.isNotEmpty()) savedItems.size.toString() else null,
                    leading = { Tile("🗂️", c.bg) },
                    trailing = { Chevron() },
                    onClick = onSavedClick,
                )
            }
        }
    }
}

@Composable
private fun Tile(emoji: String, bg: Color) {
    Box(Modifier.size(48.dp).background(bg, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Text(emoji, fontSize = 24.sp)
    }
}

@Composable
private fun Chevron() {
    Icon(painterResource(R.drawable.ic_chevron_right), null, tint = FinanceTheme.colors.textDisabled, modifier = Modifier.size(18.dp))
}
