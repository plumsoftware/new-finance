package ru.plumsoftware.finance.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),  // Маленькие бейджи, теги, всплывающие подсказки
    small = RoundedCornerShape(14.dp),      // Текстовые поля, небольшие кнопки (в стиле кнопок iOS)
    medium = RoundedCornerShape(20.dp),     // Основные карточки (виджеты, списки транзакций)
    large = RoundedCornerShape(24.dp),      // Нижние шторки (Bottom Sheets), крупные диалоги
    extraLarge = RoundedCornerShape(32.dp)  // Полноэкранные модальные окна
)