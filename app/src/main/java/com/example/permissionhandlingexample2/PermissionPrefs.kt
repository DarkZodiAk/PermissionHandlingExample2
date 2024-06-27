package com.example.permissionhandlingexample2

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("permissions")

    private var _state = PermissionsState()

    fun getPermissionsState(): Flow<PermissionsState> {
        val readKey = stringPreferencesKey(Permission.READ_STORAGE.name)
        val cameraKey = stringPreferencesKey(Permission.CAMERA.name)
        val usageKey = stringPreferencesKey(Permission.USAGE_STATS.name)
        return context.dataStore.data.map { prefs ->
            val readStatus = stringToPermissionStatus(prefs[readKey])
            val cameraStatus = stringToPermissionStatus(prefs[cameraKey])
            val usageStatus = stringToPermissionStatus(prefs[usageKey])

            _state = PermissionsState()
                .addPermissionStatus(Permission.READ_STORAGE, readStatus)
                .addPermissionStatus(Permission.CAMERA, cameraStatus)
                .addPermissionStatus(Permission.USAGE_STATS, usageStatus)
            _state
        }
    }

    suspend fun updatePermissionStatus(permission: Permission, permissionStatus: PermissionStatus) {
        val key = stringPreferencesKey(permission.name)
        context.dataStore.edit { preferences ->
            preferences[key] = permissionStatus.name
        }
    }

    private fun stringToPermissionStatus(value: String?): PermissionStatus {
        return value?.let { PermissionStatus.valueOf(it) } ?: PermissionStatus.NOT_REQUESTED
    }

    private fun PermissionsState.addPermissionStatus(permission: Permission, status: PermissionStatus): PermissionsState {
        return when(status) {
            PermissionStatus.NOT_REQUESTED -> {
                PermissionsState(notRequested + permission, declined, permanentlyDeclined, granted)
            }
            PermissionStatus.DECLINED -> {
                PermissionsState(notRequested, declined + permission, permanentlyDeclined, granted)
            }
            PermissionStatus.PERMANENTLY_DECLINED -> {
                PermissionsState(notRequested, declined, permanentlyDeclined + permission, granted)
            }
            PermissionStatus.GRANTED -> {
                PermissionsState(notRequested, declined, permanentlyDeclined, granted + permission)
            }
        }
    }
}