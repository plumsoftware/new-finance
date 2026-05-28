package ru.plumsoftware.finance.ui.theme

import androidx.compose.ui.graphics.Color

// Базовые цвета iOS
val IosBlue = Color(0xFF007AFF) // Синий акцент (кнопки, активные элементы)
val IosGreen = Color(0xFF34C759) // Для доходов и успешной экономии
val IosRed = Color(0xFFFF3B30)   // Для расходов
val IosOrange = Color(0xFFFF9500)
val IosPurple = Color(0xFF5856D6)
val IosViolet = Color(0xFFAF52DE)

// Светлая тема
val LightBackground = Color(0xFFF2F2F7) // Системный светло-серый фон iOS
val LightSurface = Color(0xFFFFFFFF)    // Белые карточки
val LightTextPrimary = Color(0xFF000000)
val LightTextSecondary = Color(0xFF8E8E93) // Серый текст для подписей
val LightDivider = Color(0x333C3C43) // Еле заметный разделитель
val LightSeparator = Color(0xFFE5E5EA)
val LightChevron = Color(0xFFC7C7CC)
val LightSurfaceMuted = Color(0xFFF2F2F7)

// Темная тема
val DarkBackground = Color(0xFF000000)  // Глубокий черный фон (OLED)
val DarkSurface = Color(0xFF1C1C1E)     // Темно-серые карточки (не сливаются с фоном)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.6f)
val DarkDivider = Color(0x545458A6)
val DarkSeparator = Color(0xFF38383A)
val DarkChevron = Color(0xFF636366)
val DarkSurfaceMuted = Color(0xFF2C2C2E)

// Прозрачность для стеклянной навигации
val NavBarGlassLight = Color(0xD9FFFFFF)
val NavBarGlassDark = Color(0xD91C1C1E)
