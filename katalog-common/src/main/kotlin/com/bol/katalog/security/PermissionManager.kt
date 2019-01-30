package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission

interface PermissionManager {
    suspend fun <T : Any> hasPermission(entity: T, permission: GroupPermission): Boolean
    suspend fun <T : Any> requirePermission(
        entity: T,
        permission: GroupPermission,
        throwIfNotAllowed: () -> Throwable
    ) {
        if (!hasPermission(entity, permission)) {
            throw throwIfNotAllowed()
        }
    }
}