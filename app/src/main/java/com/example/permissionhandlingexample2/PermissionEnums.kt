package com.example.permissionhandlingexample2

import android.Manifest

enum class Permission(val key: String) {
    READ_STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE),
    CAMERA(Manifest.permission.CAMERA),
    USAGE_STATS(Manifest.permission.PACKAGE_USAGE_STATS)
}

enum class PermissionStatus {
    NOT_REQUESTED, DECLINED, PERMANENTLY_DECLINED, GRANTED
}