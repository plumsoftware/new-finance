package ru.plumsoftware.finance.presentation.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.data.config.SavedCalculationsRepository
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.presentation.common.NBSP
import ru.plumsoftware.finance.ui.ds.ButtonTonal
import ru.plumsoftware.finance.ui.ds.FCard
import ru.plumsoftware.finance.ui.ds.HSpace
import ru.plumsoftware.finance.ui.ds.InkCard
import ru.plumsoftware.finance.ui.ds.Kopi
import ru.plumsoftware.finance.ui.ds.LocalMascotSnackbar
import ru.plumsoftware.finance.ui.ds.MascotTip
import ru.plumsoftware.finance.ui.ds.StackedBar
import ru.plumsoftware.finance.ui.ds.SubScreenAppBar
import ru.plumsoftware.finance.ui.ds.VSpace
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToLong

/** Цвета компонентов составной полосы (§6.8 п.2). */
object CalcColors {
    val Principal = Color(0xFF007AFF)
    val Overpay = Color(0xFFFF6B63)
    val Interest = Color(0xFF5BE07F)
    val Contributions = Color(0xFF5B8DEF)
    val Have = Color(0xFFFFD60A)
    val Withdrawn = Color(0xFFFF9F0A)
}

data class LegendItem(val color: Color, val label: String, val value: String)

/** Итог расчёта для тёмного блока и «Сохранить расчёт». */
data class CalcResult(
    val label: String,
    val value: String,
    val secondLine: String? = null,
    val parts: List<Pair<Float, Color>> = emptyList(),
    val legend: List<LegendItem> = emptyList(),
)

/** Форматтер «12 мес.», «5 лет» для лямбд слайдеров (не @Composable). */
@Composable
fun rememberPluralFormatter(pluralRes: Int): (Int) -> String {
    val res = androidx.compose.ui.platform.LocalContext.current.resources
    return remember(pluralRes, res) { { n: Int -> res.getQuantityString(pluralRes, n, n) } }
}

/** Рубли → строка «1 234 ₽» (целые). */
fun rub(value: Double): String = Money.formatRounded((value * 100).roundToLong())

/** Формат числа параметра: «1 500 000», «12,5». */
fun fmtNumber(value: Double, decimals: Int = 0): String {
    val bd = BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP)
    val whole = Money.groupThousands(bd.toLong())
    return if (decimals == 0) whole else {
        val frac = bd.remainder(BigDecimal.ONE).abs().movePointRight(decimals).toLong()
        if (frac == 0L) whole else "$whole,${frac.toString().padStart(decimals, '0').trimEnd('0')}"
    }
}

/**
 * Шаблон экрана калькулятора: итог всегда вверху, параметры, подсказка Коппи, «Сохранить расчёт».
 * Пересчёт — при каждом изменении слайдера, без кнопки «Рассчитать».
 */
@Composable
fun CalculatorScaffold(
    title: String,
    emoji: String,
    calculatorKey: String,
    onBack: () -> Unit,
    result: CalcResult,
    tip: String?,
    savedDetails: String = "",
    saved: SavedCalculationsRepository = koinInject(),
    params: @Composable ColumnScope.() -> Unit,
) {
    val c = FinanceTheme.colors
    val snackbar = LocalMascotSnackbar.current
    val savedMsg = stringResource(R.string.calc_saved)
    Column(Modifier.fillMaxSize().background(c.bg)) {
        SubScreenAppBar(title = title, onBack = onBack, actions = { Text(emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp)) })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ResultBlock(result)
            FCard { Column(verticalArrangement = Arrangement.spacedBy(18.dp), content = params) }
            if (tip != null) {
                FCard(padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
                    MascotTip(text = tip, mascotSize = 40.dp, bubbleColor = c.bg)
                }
            }
            ButtonTonal(
                stringResource(R.string.calc_save),
                onClick = {
                    saved.add(calculatorKey, title, "${result.label}: ${result.value}", savedDetails)
                    snackbar.show(savedMsg, Kopi.HAPPY)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            VSpace(16.dp)
        }
    }
}

@Composable
private fun ResultBlock(r: CalcResult) {
    val c = FinanceTheme.colors
    InkCard(radius = 26.dp) {
        Text(r.label, style = FinanceType.bodySmall, color = c.onInkSecondary)
        Text(r.value, style = FinanceType.displayResult, color = Color.White)
        if (r.secondLine != null) Text(r.secondLine, style = FinanceType.bodySmall, color = c.onInkSecondary)
        if (r.parts.isNotEmpty()) {
            VSpace(14.dp)
            StackedBar(r.parts)
        }
        if (r.legend.isNotEmpty()) {
            VSpace(14.dp)
            r.legend.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    row.forEach { item ->
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).background(item.color, RoundedCornerShape(2.dp)))
                                HSpace(6.dp)
                                Text(item.label, style = FinanceType.caption, color = c.onInkSecondary)
                            }
                            Text(item.value, style = FinanceType.titleBold, color = Color.White)
                        }
                    }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Параметр-слайдер: подпись слева, значение справа (тап — точный ввод), Material 3 Slider `primary`.
 */
@Composable
fun SliderParam(
    label: String,
    value: Double,
    onChange: (Double) -> Unit,
    min: Double,
    max: Double,
    step: Double,
    format: (Double) -> String,
    decimals: Int = 0,
) {
    val c = FinanceTheme.colors
    var showInput by remember { mutableStateOf(false) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = FinanceType.bodySmall, color = c.textSecondary, modifier = Modifier.weight(1f))
            Text(
                format(value),
                style = FinanceType.titleBold,
                color = c.textPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button) { showInput = true }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
        Slider(
            value = value.toFloat().coerceIn(min.toFloat(), max.toFloat()),
            onValueChange = { v -> onChange(snap(v.toDouble(), min, max, step)) },
            valueRange = min.toFloat()..max.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = c.primary,
                activeTrackColor = c.primary,
                inactiveTrackColor = c.trackMuted,
            ),
        )
    }
    if (showInput) {
        ExactInputDialog(label, value, min, max, decimals, onDismiss = { showInput = false }) {
            onChange(it.coerceIn(min, max))
            showInput = false
        }
    }
}

fun snap(v: Double, min: Double, max: Double, step: Double): Double {
    val steps = ((v - min) / step).roundToLong()
    return (min + steps * step).coerceIn(min, max).let { BigDecimal.valueOf(it).setScale(4, RoundingMode.HALF_UP).toDouble() }
}

@Composable
private fun ExactInputDialog(
    label: String,
    value: Double,
    min: Double,
    max: Double,
    decimals: Int,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    val c = FinanceTheme.colors
    var text by remember { mutableStateOf(fmtNumber(value, decimals).replace(NBSP.toString(), "")) }
    val parsed = text.replace(" ", "").replace(',', '.').toDoubleOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(label, style = FinanceType.titleLarge, color = c.textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' }.take(14) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                VSpace(6.dp)
                Text(
                    stringResource(R.string.calc_range_hint, fmtNumber(min, decimals), fmtNumber(max, decimals)),
                    style = FinanceType.caption,
                    color = c.textSecondary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { parsed?.let(onConfirm) }, enabled = parsed != null) {
                Text(stringResource(R.string.save), color = c.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = c.textSecondary) } },
    )
}
