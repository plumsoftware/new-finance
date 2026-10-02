package ru.plumsoftware.finance.ui.ds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

/**
 * Скрытие сумм (§6.13 «Скрывать суммы при запуске — показывать по нажатию»).
 * Состояние живёт в рамках сессии: при запуске берётся из настройки, дальше переключается касанием.
 */
@Stable
class AmountVisibility(val hidden: Boolean, val toggle: () -> Unit)

val LocalAmountVisibility = compositionLocalOf { AmountVisibility(false) {} }

const val MASK = "••••"

/** Сумма или маска, если суммы скрыты. */
@Composable
fun masked(text: String): String = if (LocalAmountVisibility.current.hidden) MASK else text
