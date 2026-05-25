package ru.plumsoftware.finance.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.plumsoftware.finance.R

val Inter18Family = FontFamily(
    Font(R.font.inter_18pt_thin, FontWeight.Thin),
    Font(R.font.inter_18pt_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.inter_18pt_extralight, FontWeight.ExtraLight),
    Font(R.font.inter_18pt_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.inter_18pt_light, FontWeight.Light),
    Font(R.font.inter_18pt_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.inter_18pt_regular, FontWeight.Normal),
    Font(R.font.inter_18pt_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.inter_18pt_medium, FontWeight.Medium),
    Font(R.font.inter_18pt_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.inter_18pt_semibold, FontWeight.SemiBold),
    Font(R.font.inter_18pt_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.inter_18pt_bold, FontWeight.Bold),
    Font(R.font.inter_18pt_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.inter_18pt_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_18pt_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.inter_18pt_black, FontWeight.Black),
    Font(R.font.inter_18pt_blackitalic, FontWeight.Black, FontStyle.Italic)
)

// 2. Семейство для ПОДЗАГОЛОВКОВ (24pt)
val Inter24Family = FontFamily(
    Font(R.font.inter_24pt_thin, FontWeight.Thin),
    Font(R.font.inter_24pt_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.inter_24pt_extralight, FontWeight.ExtraLight),
    Font(R.font.inter_24pt_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.inter_24pt_light, FontWeight.Light),
    Font(R.font.inter_24pt_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.inter_24pt_regular, FontWeight.Normal),
    Font(R.font.inter_24pt_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.inter_24pt_medium, FontWeight.Medium),
    Font(R.font.inter_24pt_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.inter_24pt_semibold, FontWeight.SemiBold),
    Font(R.font.inter_24pt_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.inter_24pt_bold, FontWeight.Bold),
    Font(R.font.inter_24pt_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.inter_24pt_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_24pt_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.inter_24pt_black, FontWeight.Black),
    Font(R.font.inter_24pt_blackitalic, FontWeight.Black, FontStyle.Italic)
)

// 3. Семейство для КРУПНЫХ ЗАГОЛОВКОВ (28pt)
val Inter28Family = FontFamily(
    Font(R.font.inter_28pt_thin, FontWeight.Thin),
    Font(R.font.inter_28pt_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.inter_28pt_extralight, FontWeight.ExtraLight),
    Font(R.font.inter_28pt_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.inter_28pt_light, FontWeight.Light),
    Font(R.font.inter_28pt_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.inter_28pt_regular, FontWeight.Normal),
    Font(R.font.inter_28pt_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.inter_28pt_medium, FontWeight.Medium),
    Font(R.font.inter_28pt_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.inter_28pt_semibold, FontWeight.SemiBold),
    Font(R.font.inter_28pt_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.inter_28pt_bold, FontWeight.Bold),
    Font(R.font.inter_28pt_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.inter_28pt_extrabold, FontWeight.ExtraBold),
    Font(R.font.inter_28pt_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.inter_28pt_black, FontWeight.Black),
    Font(R.font.inter_28pt_blackitalic, FontWeight.Black, FontStyle.Italic)
)

// 4. НАСТРОЙКА САМОЙ ТИПОГРАФИКИ (Typography)
val Typography = Typography(

    // САМОЕ КРУПНОЕ: Используем Inter28.
    // Для главного баланса на Дашборде (например: 120 000 ₽)
    displayLarge = TextStyle(
        fontFamily = Inter28Family,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1).sp // Небольшое сужение для iOS-стиля
    ),

    // ЗАГОЛОВКИ ЭКРАНОВ: Используем Inter24.
    // Например "История", "Настройки"
    titleLarge = TextStyle(
        fontFamily = Inter24Family,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.36.sp
    ),

    // ЗАГОЛОВКИ КАРТОЧЕК: Используем Inter24.
    // Например названия категорий ("Продукты", "Транспорт")
    titleMedium = TextStyle(
        fontFamily = Inter24Family,
        fontWeight = FontWeight.Medium, // Medium отлично смотрится в iOS
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.4).sp
    ),

    // ОСНОВНОЙ ТЕКСТ: Используем Inter18.
    // Суммы транзакций, кнопки, списки
    bodyLarge = TextStyle(
        fontFamily = Inter18Family,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.4).sp
    ),

    // МЕЛКИЙ ТЕКСТ: Используем Inter18.
    // Даты, подписи, проценты (серым цветом)
    bodyMedium = TextStyle(
        fontFamily = Inter18Family,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.24).sp
    ),

    // ТЕКСТ КНОПОК
    labelLarge = TextStyle(
        fontFamily = Inter18Family,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
)