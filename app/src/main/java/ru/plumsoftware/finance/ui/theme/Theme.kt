package ru.plumsoftware.finance.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LightSurface,     // В темной теме кнопки/акценты белые
    onPrimary = DarkBackground, // Текст на белой кнопке - черный
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    error = IosRed,
    secondary = IosBlue,
    onSecondary = LightSurface,
    tertiary = IosGreen,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSeparator,
    outlineVariant = DarkChevron,
    surfaceVariant = DarkSurfaceMuted,
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBackground,   // В светлой теме кнопки/акценты черные
    onPrimary = LightSurface,   // Текст на черной кнопке - белый
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    error = IosRed,
    secondary = IosBlue,
    onSecondary = LightSurface,
    tertiary = IosGreen,
    onSurfaceVariant = LightTextSecondary,
    outline = LightSeparator,
    outlineVariant = LightChevron,
    surfaceVariant = LightSurfaceMuted,
)

@Composable
fun FinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}