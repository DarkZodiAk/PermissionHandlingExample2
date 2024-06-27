package com.example.permissionhandlingexample2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val permissionPrefs: PermissionPrefs
) : ViewModel() {

    val state = permissionPrefs.getPermissionsState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PermissionsState(notRequested = Permission.entries))

    fun updatePermissionStatus(args: PermissionStatusArgs) {
        viewModelScope.launch {
            if (args.isGranted) {
                permissionPrefs.updatePermissionStatus(args.permission, PermissionStatus.GRANTED)
            } else if (args.shouldShowRationale) {
                permissionPrefs.updatePermissionStatus(args.permission, PermissionStatus.DECLINED)
            } else if (args.permission !in state.value.notRequested) {
                permissionPrefs.updatePermissionStatus(args.permission, PermissionStatus.PERMANENTLY_DECLINED)
            } else {
                permissionPrefs.updatePermissionStatus(args.permission, PermissionStatus.NOT_REQUESTED)
            }
        }
    }
}

data class PermissionStatusArgs(
    val permission: Permission,
    val isGranted: Boolean,
    val shouldShowRationale: Boolean
)