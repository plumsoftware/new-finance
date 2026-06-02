package ru.plumsoftware.finance.presentation.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import ru.plumsoftware.finance.util.isBackgroundWorkAllowed

fun buildPermissionsToRequest(context: Context): List<String> {
    val list = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!notifGranted) {
            list += Manifest.permission.POST_NOTIFICATIONS
        }
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val storageGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
        if (!storageGranted) {
            list += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
    }
    return list
}

fun hasPendingPermissions(context: Context): Boolean =
    buildPermissionsToRequest(context).isNotEmpty() || !isBackgroundWorkAllowed(context)

fun isNotificationPermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

fun isStoragePermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) == PackageManager.PERMISSION_GRANTED
}
