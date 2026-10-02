package ru.plumsoftware.finance.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Типографика редизайна (ТЗ §3.2). Цифры табличные (`tnum`). */
object FinanceType {
    private fun style(
        size: TextUnit,
        line: TextUnit,
        weight: FontWeight,
        tracking: TextUnit = 0.sp,
    ) = TextStyle(
        fontFamily = if (size.value >= 28f) Inter28Family else if (size.value >= 18f) Inter24Family else Inter18Family,
        fontSize = size,
        lineHeight = line,
        fontWeight = weight,
        letterSpacing = tracking,
        fontFeatureSettings = "tnum",
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    )

    val displayHero = style(46.sp, 54.sp, FontWeight.ExtraBold, (-1.8).sp)
    val displayResult = style(36.sp, 42.sp, FontWeight.ExtraBold, (-1.2).sp)
    val displayAmount = style(46.sp, 54.sp, FontWeight.Bold, (-1.2).sp)
    val headlineLarge = style(34.sp, 40.sp, FontWeight.Bold, (-0.8).sp)
    val headline = style(28.sp, 34.sp, FontWeight.Bold, (-0.5).sp)
    val titleLarge = style(20.sp, 26.sp, FontWeight.SemiBold)
    val titleSection = style(18.sp, 24.sp, FontWeight.Bold)
    val title = style(16.sp, 22.sp, FontWeight.SemiBold)
    val titleBold = style(16.sp, 22.sp, FontWeight.Bold)
    val body = style(15.sp, 20.sp, FontWeight.Normal)
    val bodyMedium = style(15.sp, 20.sp, FontWeight.Medium)
    val bodySmall = style(14.sp, 20.sp, FontWeight.Normal)
    val label = style(13.sp, 18.sp, FontWeight.SemiBold, 0.6.sp)
    val caption = style(12.sp, 16.sp, FontWeight.Normal)
    val captionBold = style(12.sp, 16.sp, FontWeight.SemiBold)
    val micro = style(11.sp, 14.sp, FontWeight.Medium)
    val axis = style(10.sp, 12.sp, FontWeight.Medium)
}
