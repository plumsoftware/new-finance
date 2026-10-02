package ru.plumsoftware.finance.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun FinanceColors.toDarkScheme() = darkColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primaryTonalBg,
    onPrimaryContainer = primaryTonalText,
    secondary = success,
    onSecondary = Color.White,
    tertiary = primary,
    background = bg,
    onBackground = textPrimary,
    surface = surface,
    onSurface = textPrimary,
    surfaceContainer = surface,
    surfaceContainerLow = surface,
    surfaceContainerHigh = surface,
    surfaceVariant = trackMuted,
    onSurfaceVariant = textSecondary,
    error = danger,
    outline = outline,
    outlineVariant = textDisabled,
    scrim = scrim,
)

private fun FinanceColors.toLightScheme() = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primaryTonalBg,
    onPrimaryContainer = primaryTonalText,
    secondary = success,
    onSecondary = Color.White,
    tertiary = primary,
    background = bg,
    onBackground = textPrimary,
    surface = surface,
    onSurface = textPrimary,
    surfaceContainer = surface,
    surfaceContainerLow = surface,
    surfaceContainerHigh = surface,
    surfaceVariant = trackMuted,
    onSurfaceVariant = textSecondary,
    error = danger,
    outline = outline,
    outlineVariant = textDisabled,
    scrim = scrim,
)

private val DarkColorScheme = DarkFinanceColors.toDarkScheme()
private val LightColorScheme = LightFinanceColors.toLightScheme()

/** Доступ к токенам редизайна: `FinanceTheme.colors`. */
object FinanceTheme {
    val colors: FinanceColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinanceColors.current
}

@Composable
fun FinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalFinanceColors provides if (darkTheme) DarkFinanceColors else LightFinanceColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}
