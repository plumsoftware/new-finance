package ru.plumsoftware.finance.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.presentation.common.Money
import ru.plumsoftware.finance.ui.theme.FinanceTheme
import ru.plumsoftware.finance.ui.theme.FinanceType

/**
 * Логика ввода суммы (§6.2 п.7): не больше 9 цифр целой части, 2 знака после запятой, одна запятая.
 * Строка хранится в виде «1234,5».
 */
object AmountInput {
    const val MAX_INT_DIGITS = 9
    const val MAX_FRACTION_DIGITS = 2
    const val BACKSPACE = "⌫"
    const val COMMA = ","

    fun press(current: String, key: String): String = when (key) {
        BACKSPACE -> current.dropLast(1)
        COMMA -> when {
            current.contains(',') -> current
            current.isEmpty() -> "0,"
            else -> "$current,"
        }
        else -> {
            val digit = key.singleOrNull()?.takeIf { it.isDigit() } ?: return current
            val parts = current.split(',')
            if (parts.size == 2) {
                if (parts[1].length >= MAX_FRACTION_DIGITS) current else current + digit
            } else {
                when {
                    current == "0" -> digit.toString()
                    current.length >= MAX_INT_DIGITS -> current
                    else -> current + digit
                }
            }
        }
    }

    /** Строка ввода → копейки. */
    fun toMinor(input: String): Long {
        if (input.isEmpty()) return 0L
        val parts = input.split(',')
        val whole = parts[0].ifEmpty { "0" }.toLong()
        val frac = parts.getOrNull(1).orEmpty().padEnd(2, '0').take(2).ifEmpty { "00" }.toLong()
        return whole * 100 + frac
    }

    /** Копейки → строка ввода. */
    fun fromMinor(minor: Long): String {
        if (minor <= 0) return ""
        val whole = minor / 100
        val frac = minor % 100
        return if (frac == 0L) whole.toString()
        else "$whole,${frac.toString().padStart(2, '0').trimEnd('0')}"
    }

    /** Отображение с разделителями тысяч: «12 345,5». */
    fun display(input: String): String {
        if (input.isEmpty()) return "0"
        val parts = input.split(',')
        val whole = Money.groupThousands(parts[0].ifEmpty { "0" }.toLong())
        return if (parts.size == 2) "$whole,${parts[1]}" else whole
    }
}

/** Цифровая клавиатура 3×4 (§6.2 п.7): клавиши 52dp, радиус 16, белые, нажатие — `#E3E4EA`. */
@Composable
fun AmountKeypad(
    onKey: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(AmountInput.COMMA, "0", AmountInput.BACKSPACE),
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key -> KeypadKey(key, onKey, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun KeypadKey(key: String, onKey: (String) -> Unit, modifier: Modifier) {
    val c = FinanceTheme.colors
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val description = if (key == AmountInput.BACKSPACE) stringResource(R.string.ds_key_backspace) else key
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (pressed) c.keyPressed else c.surface)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClickLabel = description) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onKey(key)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (key == AmountInput.BACKSPACE) {
            Icon(
                painter = painterResource(R.drawable.ic_backspace),
                contentDescription = description,
                tint = c.textPrimary,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Text(key, style = FinanceType.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Medium), color = c.textPrimary)
        }
    }
}
