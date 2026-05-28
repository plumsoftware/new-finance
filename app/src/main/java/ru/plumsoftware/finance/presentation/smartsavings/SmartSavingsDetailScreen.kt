package ru.plumsoftware.finance.presentation.smartsavings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.plumsoftware.finance.domain.model.SmartAssetStatus
import ru.plumsoftware.finance.domain.model.SmartAssetUsage
import ru.plumsoftware.finance.presentation.common.MoneyFormat
import ru.plumsoftware.finance.ui.components.FinanceNumPad
import ru.plumsoftware.finance.ui.components.IosPrimaryButton
import ru.plumsoftware.finance.ui.components.ios.IosAlertDialog
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DetailBg = Color(0xFFF2F2F7)
private val DetailCard = Color.White
private val DetailSecondary = Color(0xFF8E8E93)
private val DetailSeparator = Color(0xFFE5E5EA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSavingsDetailScreen(
    assetId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleteSuccess: () -> Unit,
    viewModel: SmartSavingsDetailViewModel = koinViewModel { parametersOf(assetId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val asset = state.asset
    var showDeleteSheet by remember { mutableStateOf(false) }

    state.errorMessage?.let { msg ->
        IosAlertDialog(message = msg, onDismiss = viewModel::clearError)
    }

    if (state.showRecordSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::closeRecordSheet,
            sheetState = sheetState,
            containerColor = DetailBg,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0xFFC7C7CC), RoundedCornerShape(2.dp)),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Записать использование",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = MoneyFormat.formatEntryDisplay(state.amountDigits, state.currencyCode),
                        fontSize = 42.sp,
                        lineHeight = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosGreen,
                    )
                    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "cursor_alpha",
                    )
                    Text(
                        text = "|",
                        fontSize = 36.sp,
                        color = IosGreen,
                        modifier = Modifier.alpha(cursorAlpha),
                    )
                }
                Text(
                    text = "По умолчанию – ваша стандартная экономия",
                    fontSize = 13.sp,
                    color = DetailSecondary,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                    textAlign = TextAlign.Center,
                )
                FinanceNumPad(
                    onDigit = viewModel::appendDigit,
                    onBackspace = viewModel::backspace,
                )
                Spacer(Modifier.height(14.dp))
                IosPrimaryButton(
                    text = "Подтвердить",
                    onClick = viewModel::recordSaving,
                    enabled = MoneyFormat.majorDigitsToMinor(state.amountDigits, state.currencyCode) > 0L,
                    loading = state.isSaving,
                )
            }
        }
    }

    if (showDeleteSheet && asset != null) {
        val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = deleteSheetState,
            containerColor = DetailBg,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Удалить «${asset.name}»?",
                    fontSize = 17.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Отмена",
                    color = IosBlue,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteSheet = false }
                        .padding(12.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Удалить",
                    color = IosRed,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDeleteSheet = false
                            viewModel.deleteAsset(onDeleted = onDeleteSuccess)
                        }
                        .padding(12.dp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DetailBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onBack)
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = null,
                        tint = IosBlue,
                        modifier = Modifier.size(18.dp),
                    )
                    Text("Экономия", color = IosBlue, fontSize = 17.sp)
                }
                Text(
                    text = asset?.name.orEmpty(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = { onEdit(assetId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = IosBlue, modifier = Modifier.size(22.dp))
                    }
                }
            }
        },
    ) { padding ->
        if (asset == null) {
            Text(
                text = "Актив не найден",
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp),
            )
            return@Scaffold
        }

        val animatedProgress by animateFloatAsState(
            targetValue = asset.paybackProgress.coerceIn(0f, 1f),
            animationSpec = tween(600),
            label = "asset_progress",
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(IosBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(asset.icon, fontSize = 44.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = asset.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                    Text(
                        text = "+${MoneyFormat.format(asset.totalSavedMinor, state.currencyCode)}",
                        fontSize = 36.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosGreen,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = "всего сэкономлено",
                        fontSize = 14.sp,
                        color = DetailSecondary,
                    )
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DetailCard,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Окупаемость", fontSize = 15.sp, color = DetailSecondary)
                            Text(
                                "${(animatedProgress * 100).toInt()}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (asset.status == SmartAssetStatus.PROFIT) IosGreen else IosBlue,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(10.dp),
                            color = if (asset.status == SmartAssetStatus.PROFIT) IosGreen else IosBlue,
                            trackColor = DetailSeparator,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = if (asset.status == SmartAssetStatus.PROFIT) {
                                "🎉 Окупился! Чистая прибыль"
                            } else {
                                "Осталось окупить: ${MoneyFormat.format((asset.purchaseCostMinor - asset.totalSavedMinor).coerceAtLeast(0), state.currencyCode)}"
                            },
                            fontSize = 13.sp,
                            color = if (asset.status == SmartAssetStatus.PROFIT) IosGreen else DetailSecondary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCell(
                        label = "Стоимость покупки",
                        value = MoneyFormat.format(asset.purchaseCostMinor, state.currencyCode),
                        valueColor = Color.Black,
                        modifier = Modifier.weight(1f),
                    )
                    StatCell(
                        label = "Экономия за раз",
                        value = MoneyFormat.format(asset.alternativeCostMinor, state.currencyCode),
                        valueColor = IosGreen,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                IosPrimaryButton(
                    text = "✓ Использовал – записать экономию",
                    onClick = viewModel::openRecordSheet,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            item {
                Text(
                    text = "ИСТОРИЯ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DetailSecondary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DetailCard,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.usages.isEmpty()) {
                        Text(
                            text = "Ещё нет записей",
                            fontSize = 15.sp,
                            color = DetailSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Column {
                            state.usages.forEachIndexed { index, usage ->
                                UsageRow(usage, state.currencyCode)
                                if (index != state.usages.lastIndex) {
                                    HorizontalDivider(
                                        color = DetailSeparator,
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(start = 14.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    text = "Удалить актив",
                    color = IosRed,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteSheet = true }
                        .padding(top = 16.dp, bottom = 34.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DetailCard,
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, fontSize = 12.sp, color = DetailSecondary)
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun UsageRow(usage: SmartAssetUsage, currencyCode: String) {
    val date = SimpleDateFormat("d MMMM, HH:mm", Locale("ru")).format(Date(usage.usedAtMillis))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = date, fontSize = 15.sp, color = DetailSecondary)
        Text(
            text = "+${MoneyFormat.format(usage.savedAmountMinor, currencyCode)}",
            color = IosGreen,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
