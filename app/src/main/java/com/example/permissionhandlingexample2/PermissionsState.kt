package com.example.permissionhandlingexample2

data class PermissionsState(
    val notRequested: List<Permission> = emptyList(),
    val declined: List<Permission> = emptyList(),
    val permanentlyDeclined: List<Permission> = emptyList(),
    val granted: List<Permission> = emptyList()
)
