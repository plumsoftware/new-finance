package ru.plumsoftware.finance.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Дизайн-токены редизайна (ТЗ §3.1). Задаются парами light/dark (§13).
 * Доступ из UI: `FinanceTheme.colors`.
 */
@Immutable
data class FinanceColors(
    val bg: Color,
    val surface: Color,
    val ink: Color,
    val primary: Color,
    val primaryTonalBg: Color,
    val primaryTonalText: Color,
    val navIndicator: Color,
    val navInactive: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val divider: Color,
    val outline: Color,
    val trackMuted: Color,
    val segmentTrackOnSurface: Color,
    val segmentTrackOnBg: Color,
    val searchField: Color,
    val success: Color,
    val successText: Color,
    val warning: Color,
    val warningText: Color,
    val danger: Color,
    val dangerText: Color,
    val onInkSecondary: Color,
    val onInkSuccess: Color,
    val onInkWarning: Color,
    val onInkDangerText: Color,
    val onInkDanger: Color,
    val onInkBar: Color,
    val barDefault: Color,
    val streakBg: Color,
    val streakText: Color,
    val toastBg: Color,
    val scrim: Color,
    val navBorder: Color,
    val keyPressed: Color,
    val selectedCardBg: Color,
    val dashedBorder: Color,
    val switchTrackOff: Color,
    val switchThumbOff: Color,
    val excludedText: Color,
    val isDark: Boolean,
)

val LightFinanceColors = FinanceColors(
    bg = Color(0xFFF2F3F7),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF0B1E3F),
    primary = Color(0xFF007AFF),
    primaryTonalBg = Color(0xFFE3EDFF),
    primaryTonalText = Color(0xFF0B57D0),
    navIndicator = Color(0xFFDCE8FF),
    navInactive = Color(0xFF44464F),
    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF6E6E76),
    textDisabled = Color(0xFFC7C7CC),
    divider = Color(0xFFF2F3F7),
    outline = Color(0xFFD8D8DD),
    trackMuted = Color(0xFFE5E5EA),
    segmentTrackOnSurface = Color(0xFFF2F3F7),
    segmentTrackOnBg = Color(0xFFE3E4EA),
    searchField = Color(0xFFE6E7ED),
    success = Color(0xFF34C759),
    successText = Color(0xFF248A3D),
    warning = Color(0xFFFF9500),
    warningText = Color(0xFFC25E00),
    danger = Color(0xFFFF3B30),
    dangerText = Color(0xFFD70015),
    onInkSecondary = Color(0xFFA9B6CC),
    onInkSuccess = Color(0xFF5BE07F),
    onInkWarning = Color(0xFFFF9F0A),
    onInkDangerText = Color(0xFFFF8A84),
    onInkDanger = Color(0xFFFF6B63),
    onInkBar = Color(0xFF5B8DEF),
    barDefault = Color(0xFFB9CFF7),
    streakBg = Color(0xFFFFF1DF),
    streakText = Color(0xFFB35900),
    toastBg = Color(0xFF2B2D33),
    scrim = Color(0x660A0F1E),
    navBorder = Color(0xFFE3E4EA),
    keyPressed = Color(0xFFE3E4EA),
    selectedCardBg = Color(0xFFF0F6FF),
    dashedBorder = Color(0xFFA9C4F5),
    switchTrackOff = Color(0xFFE3E3E8),
    switchThumbOff = Color(0xFF8E8E93),
    excludedText = Color(0xFF8E8E93),
    isDark = false,
)

/** Предварительная тёмная палитра (§13): фон/поверхности/текст заменены, акценты без изменений. */
val DarkFinanceColors = LightFinanceColors.copy(
    bg = Color(0xFF0F1115),
    surface = Color(0xFF1A1D23),
    ink = Color(0xFF22324F),
    primaryTonalBg = Color(0xFF1F2E4A),
    primaryTonalText = Color(0xFF8AB4FF),
    navIndicator = Color(0xFF243656),
    navInactive = Color(0xFFA0A4AE),
    textPrimary = Color(0xFFF2F3F7),
    textSecondary = Color(0xFFA0A4AE),
    textDisabled = Color(0xFF4A4E57),
    divider = Color(0xFF262A31),
    outline = Color(0xFF3A3F48),
    trackMuted = Color(0xFF2C3038),
    segmentTrackOnSurface = Color(0xFF262A31),
    segmentTrackOnBg = Color(0xFF22262D),
    searchField = Color(0xFF22262D),
    successText = Color(0xFF5BE07F),
    warningText = Color(0xFFFF9F0A),
    dangerText = Color(0xFFFF6B63),
    streakBg = Color(0xFF3A2A14),
    streakText = Color(0xFFFFB35C),
    navBorder = Color(0xFF262A31),
    keyPressed = Color(0xFF2C3038),
    selectedCardBg = Color(0xFF1F2E4A),
    dashedBorder = Color(0xFF3D5A8A),
    switchTrackOff = Color(0xFF3A3F48),
    excludedText = Color(0xFF6E727B),
    isDark = true,
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

/** Цвета категорий по умолчанию (§3.1): эмодзи на подложке цвета с прозрачностью 13%. */
object CategoryPalette {
    const val TINT_ALPHA = 0x22 / 255f
    fun tint(color: Color): Color = color.copy(alpha = TINT_ALPHA)
}
