package ru.plumsoftware.finance.presentation.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppLockViewModel : ViewModel() {
    var isLocked by mutableStateOf(false)
        private set

    var appWasInBackground by mutableStateOf(false)
        private set

    fun onPause() {
        appWasInBackground = true
    }

    fun lock() {
        isLocked = true
    }

    fun unlock() {
        isLocked = false
        appWasInBackground = false
    }
}
