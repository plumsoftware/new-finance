package ru.plumsoftware.finance.presentation.permissions

import android.app.Activity
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import ru.plumsoftware.finance.domain.model.PermissionItem
import ru.plumsoftware.finance.domain.repository.PermissionsRepository

class PermissionsViewModel(
    private val permissionsRepository: PermissionsRepository,
) : ViewModel() {

    val items: StateFlow<List<PermissionItem>> = permissionsRepository.permissionStates

    fun refresh(activity: Activity? = null) {
        permissionsRepository.refresh(activity)
    }
}
