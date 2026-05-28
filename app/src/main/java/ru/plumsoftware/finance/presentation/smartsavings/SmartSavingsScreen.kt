package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.plumsoftware.finance.domain.model.SmartAsset
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.MascotAssets
import ru.plumsoftware.finance.ui.components.MascotEmptyState

private val SmartBg = Color(0xFFF2F2F7)
private val SmartCard = Color.White
private val SmartTextPrimary = Color(0xFF000000)
private val SmartTextSecondary = Color(0xFF8E8E93)
private val SmartSeparator = Color(0xFFE5E5EA)
private val SmartGreen = Color(0xFF34C759)
private val SmartChevron = Color(0xFFC7C7CC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSavingsScreen(
    onCreateClick: () -> Unit,
    onAssetClick: (Long) -> Unit,
    snackbarMessage: String? = null,
    onSnackbarShown: () -> Unit = {},
    viewModel: SmartSavingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val allAssets = state.payingOff + state.profit
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHost.showSnackbar(it)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SmartBg,
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = IosBlue,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить актив")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Умная экономия",
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = SmartTextPrimary,
                )
            }
            item {
                HeroCard(totalSaved = MoneyFormat.format(state.totalSavedMinor, state.currencyCode))
            }
            item {
                Text(
                    text = "МОИ АКТИВЫ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    color = SmartTextSecondary,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                )
            }

            if (allAssets.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 44.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        MascotEmptyState(
                            mascotRes = MascotAssets.emptySmartSavings,
                            title = "Ещё нет активов",
                            subtitle = "Добавьте первый предмет и следите, как он окупается",
                        )
                        Spacer(Modifier.height(24.dp))
                        IosPrimaryButton(
                            text = "Добавить актив",
                            onClick = onCreateClick,
                            modifier = Modifier.padding(horizontal = 12.dp),
                        )
                    }
                }
            } else {
                items(allAssets, key = { it.id }) { asset ->
                    SmartAssetRow(
                        asset = asset,
                        currencyCode = state.currencyCode,
                        onClick = { onAssetClick(asset.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(totalSaved: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SmartCard,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "💰  Всего сэкономлено", fontSize = 13.sp, color = SmartTextSecondary)
            Text(
                text = "+$totalSaved",
                fontSize = 38.sp,
                lineHeight = 42.sp,
                fontWeight = FontWeight.Bold,
                color = SmartGreen,
            )
            HorizontalDivider(color = SmartSeparator, thickness = 0.5.dp)
            Text(
                text = "💡 Каждое использование вместо кофе-автомата экономит деньги",
                fontSize = 13.sp,
                color = SmartTextSecondary,
            )
        }
    }
}

@Composable
private fun SmartAssetRow(
    asset: SmartAsset,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val progress = asset.paybackProgress.coerceIn(0f, 1f)
    val isProfit = asset.status == SmartAssetStatus.PROFIT
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SmartCard,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SmartBg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = asset.icon, fontSize = 26.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = SmartTextPrimary)
                Text(
                    text = "${MoneyFormat.format(asset.totalSavedMinor, currencyCode)} из ${MoneyFormat.format(asset.purchaseCostMinor, currencyCode)}",
                    fontSize = 14.sp,
                    color = SmartGreen,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(Modifier.height(10.dp))
                if (isProfit) {
                    Box(
                        modifier = Modifier
                            .background(SmartGreen.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("✓ Окупился", fontSize = 12.sp, color = SmartGreen, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = IosBlue,
                        trackColor = SmartSeparator,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = SmartTextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SmartChevron,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp),
            )
        }
    }
}
