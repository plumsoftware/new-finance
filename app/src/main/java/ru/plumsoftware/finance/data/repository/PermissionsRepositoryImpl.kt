package ru.plumsoftware.finance.data.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.plumsoftware.finance.R
import ru.plumsoftware.finance.domain.model.PermissionIconKind
import ru.plumsoftware.finance.domain.model.PermissionItem
import ru.plumsoftware.finance.domain.repository.PermissionsRepository

class PermissionsRepositoryImpl(
    private val context: Context,
) : PermissionsRepository {

    private val _permissionStates = MutableStateFlow(buildPermissionItems(null))
    override val permissionStates: StateFlow<List<PermissionItem>> = _permissionStates.asStateFlow()

    override fun refresh(activity: Activity?) {
        _permissionStates.value = buildPermissionItems(activity)
    }

    private fun buildPermissionItems(activity: Activity?): List<PermissionItem> {
        val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
        val storagePermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        } else {
            null
        }

        return listOf(
            PermissionItem(
                id = "notifications",
                permission = notifPermission,
                titleRes = R.string.perm_notif_title,
                descriptionRes = R.string.perm_notif_desc,
                rationaleRes = R.string.perm_notif_rationale,
                iconKind = PermissionIconKind.NOTIFICATIONS,
                iconColorArgb = 0xFF007AFF,
                isGranted = notifPermission?.let { isGranted(it) } ?: true,
                isPermanentlyDenied = notifPermission?.let {
                    isPermanentlyDenied(activity, it)
                } ?: false,
            ),
            PermissionItem(
                id = "storage",
                permission = storagePermission,
                titleRes = R.string.perm_storage_title,
                descriptionRes = R.string.perm_storage_desc,
                rationaleRes = R.string.perm_storage_rationale,
                iconKind = PermissionIconKind.STORAGE,
                iconColorArgb = 0xFFFF9500,
                isGranted = storagePermission?.let { isGranted(it) } ?: true,
                isPermanentlyDenied = storagePermission?.let {
                    isPermanentlyDenied(activity, it)
                } ?: false,
            ),
        )
    }

    private fun isGranted(permission: String): Boolean {
        if (permission == Manifest.permission.POST_NOTIFICATIONS &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            return true
        }
        if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            return true
        }
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun isPermanentlyDenied(activity: Activity?, permission: String): Boolean {
        if (isGranted(permission)) return false
        if (activity == null) return false
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
}
