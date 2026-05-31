package ru.plumsoftware.finance.domain.model

import androidx.annotation.StringRes

enum class PermissionIconKind {
    NOTIFICATIONS,
    STORAGE,
}

data class PermissionItem(
    val id: String,
    val permission: String?,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val rationaleRes: Int,
    val iconKind: PermissionIconKind,
    val iconColorArgb: Long,
    val isGranted: Boolean,
    val isPermanentlyDenied: Boolean,
)
