package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission

class TestPermissionManager : PermissionManager {
    override suspend fun <T> hasPermission(entity: T, permission: GroupPermission) = true
}
