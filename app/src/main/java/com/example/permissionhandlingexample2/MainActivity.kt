package com.example.permissionhandlingexample2

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.ComponentActivity.APP_OPS_SERVICE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.permissionhandlingexample2.ui.theme.PermissionHandlingExample2Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var originRequestOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PermissionHandlingExample2Theme {
                val viewModel: MainViewModel = hiltViewModel()
                val state = viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = true) {
                    viewModel.updatePermissionStatus(buildStatusArgs(Permission.READ_STORAGE))
                    viewModel.updatePermissionStatus(buildStatusArgs(Permission.CAMERA))
                    viewModel.updatePermissionStatus(buildStatusArgs(Permission.USAGE_STATS, false))
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    restoreOrientation()
                    it.forEach {
                        when(it.key) {
                            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                                viewModel.updatePermissionStatus(buildStatusArgs(Permission.READ_STORAGE))
                            }
                            Manifest.permission.CAMERA -> {
                                viewModel.updatePermissionStatus(buildStatusArgs(Permission.CAMERA))
                            }
                        }
                    }
                }

                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.updatePermissionStatus(buildStatusArgs(Permission.READ_STORAGE))
                    viewModel.updatePermissionStatus(buildStatusArgs(Permission.CAMERA))
                }

                val usageSettingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.updatePermissionStatus(buildStatusArgs(Permission.USAGE_STATS, false))
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    PermissionCard(
                        permission = Permission.READ_STORAGE,
                        isGranted = Permission.READ_STORAGE in state.value.granted,
                        isPermanentlyDeclined = Permission.READ_STORAGE in state.value.permanentlyDeclined,
                        onRequest = {
                            lockOrientation()
                            permissionLauncher.launch(arrayOf(Permission.READ_STORAGE.key))
                        },
                        onGoToSettings = { settingsLauncher.launch(getAppDetailsIntent()) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PermissionCard(
                        permission = Permission.CAMERA,
                        isGranted = Permission.CAMERA in state.value.granted,
                        isPermanentlyDeclined = Permission.CAMERA in state.value.permanentlyDeclined,
                        onRequest = {
                            lockOrientation()
                            permissionLauncher.launch(arrayOf(Permission.CAMERA.key))
                        },
                        onGoToSettings = { settingsLauncher.launch(getAppDetailsIntent()) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PermissionCard(
                        permission = Permission.USAGE_STATS,
                        isGranted = Permission.USAGE_STATS in state.value.granted,
                        isPermanentlyDeclined = false,
                        onRequest = { usageSettingsLauncher.launch(getUsageStatsIntent()) },
                        onGoToSettings = null
                    )
                }
            }
        }
    }

    private fun lockOrientation() {
        originRequestOrientation = requestedOrientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }

    private fun restoreOrientation() {
        requestedOrientation = originRequestOrientation
    }
}

fun ComponentActivity.buildStatusArgs(permission: Permission, shouldShowRationale: Boolean? = null): PermissionStatusArgs {
    return PermissionStatusArgs(
        permission,
        hasPermission(permission),
        shouldShowRationale ?: shouldShowPermissionRationale(permission)
    )
}

fun Context.getAppDetailsIntent(): Intent {
    return Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
}

fun Context.getUsageStatsIntent(): Intent {
    return Intent(
        Settings.ACTION_USAGE_ACCESS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    )
}

fun Context.hasPermission(permission: Permission): Boolean {
    return when(permission) {
        Permission.USAGE_STATS -> hasUsageStatisticsPermission()
        else -> checkSelfPermission(permission.key) == PackageManager.PERMISSION_GRANTED
    }
}

fun ComponentActivity.shouldShowPermissionRationale(permission: Permission): Boolean {
    return when(permission) {
        Permission.USAGE_STATS -> throw(PermissionException())
        else -> shouldShowRequestPermissionRationale(permission.key)
    }
}

fun Context.hasUsageStatisticsPermission(): Boolean {
    val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
    val mode = if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
    } else {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}