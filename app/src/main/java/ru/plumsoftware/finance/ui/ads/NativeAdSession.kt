package ru.plumsoftware.finance.ui.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Скрытие нативной рекламы до перезапуска приложения (в памяти процесса). */
object NativeAdSession {
    var dismissed by mutableStateOf(false)
}
