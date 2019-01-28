package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission

interface PermissionManager {
    suspend fun <T> hasPermission(entity: T, permission: GroupPermission): Boolean
    suspend fun <T> requirePermission(entity: T, permission: GroupPermission, throwIfNotAllowed: () -> Throwable) {
        if (!hasPermission(entity, permission)) {
            throw throwIfNotAllowed()
        }
    }
}