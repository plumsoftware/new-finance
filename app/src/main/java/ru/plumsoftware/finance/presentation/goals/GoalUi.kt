package ru.plumsoftware.finance.presentation.goals

import androidx.compose.ui.graphics.Color
import ru.plumsoftware.finance.ui.theme.IosBlue
import ru.plumsoftware.finance.ui.theme.IosGreen
import ru.plumsoftware.finance.ui.theme.IosOrange
import ru.plumsoftware.finance.ui.theme.IosRed

val goalColorPalette = listOf(
    "#FF3B30", "#FF9500", "#FFCC00", "#34C759", "#00C7BE",
    "#30B0C7", "#007AFF", "#5856D6", "#AF52DE", "#FF2D55",
    "#8E8E93", "#A2845E", "#5AC8FA", "#64D2FF", "#BF5AF2",
    "#DAA520", "#228B22", "#4B0082", "#2E8B57", "#B22222",
)

fun colorFromHexOrDefault(hex: String, fallback: Color = IosBlue): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrElse { fallback }

fun goalDeadlineColor(isOverdue: Boolean, daysLeft: Int?, defaultColor: Color): Color = when {
    isOverdue -> IosRed
    daysLeft != null && daysLeft <= 7 -> IosOrange
    else -> defaultColor
}

fun goalAmountColor(isCompleted: Boolean): Color = if (isCompleted) IosGreen else IosRed
