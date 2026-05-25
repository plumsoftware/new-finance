package ru.plumsoftware.finance.ui.theme

import androidx.compose.ui.graphics.Color

// Базовые цвета iOS
val IosBlue = Color(0xFF007AFF) // Синий акцент (кнопки, активные элементы)
val IosGreen = Color(0xFF34C759) // Для доходов и успешной экономии
val IosRed = Color(0xFFFF3B30)   // Для расходов

// Светлая тема
val LightBackground = Color(0xFFF2F2F7) // Системный светло-серый фон iOS
val LightSurface = Color(0xFFFFFFFF)    // Белые карточки
val LightTextPrimary = Color(0xFF000000)
val LightTextSecondary = Color(0xFF8E8E93) // Серый текст для подписей
val LightDivider = Color(0x333C3C43) // Еле заметный разделитель

// Темная тема
val DarkBackground = Color(0xFF000000)  // Глубокий черный фон (OLED)
val DarkSurface = Color(0xFF1C1C1E)     // Темно-серые карточки (не сливаются с фоном)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.6f)
val DarkDivider = Color(0x545458A6)