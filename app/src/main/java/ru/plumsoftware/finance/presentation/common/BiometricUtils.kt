package ru.plumsoftware.finance.presentation.common

import android.content.Context
import androidx.biometric.BiometricManager

object BiometricUtils {
    fun isAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK,
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
