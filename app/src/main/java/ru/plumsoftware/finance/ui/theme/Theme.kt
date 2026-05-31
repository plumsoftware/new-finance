package ru.plumsoftware.finance.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = IncomeGreen,
    onSecondary = Color.White,
    tertiary = AccentBlue,
    background = BackgroundDark,
    onBackground = TextPrimaryD,
    surface = SurfaceDark,
    onSurface = TextPrimaryD,
    surfaceVariant = SeparatorDark,
    onSurfaceVariant = TextSecondaryD,
    error = ExpenseRed,
    outline = SeparatorDark,
    outlineVariant = TextPlaceholderD,
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = IncomeGreen,
    onSecondary = Color.White,
    tertiary = AccentBlue,
    background = BackgroundLight,
    onBackground = TextPrimaryL,
    surface = SurfaceLight,
    onSurface = TextPrimaryL,
    surfaceVariant = SeparatorLight,
    onSurfaceVariant = TextSecondaryL,
    error = ExpenseRed,
    outline = SeparatorLight,
    outlineVariant = TextPlaceholderL,
)

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
