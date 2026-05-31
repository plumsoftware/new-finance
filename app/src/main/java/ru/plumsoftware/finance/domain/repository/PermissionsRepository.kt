package ru.plumsoftware.finance.domain.repository

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow
import ru.plumsoftware.finance.domain.model.PermissionItem

interface PermissionsRepository {
    val permissionStates: StateFlow<List<PermissionItem>>

    fun refresh(activity: Activity? = null)
}
