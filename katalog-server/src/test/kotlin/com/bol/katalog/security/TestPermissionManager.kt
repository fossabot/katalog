package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId

class TestPermissionManager : PermissionManager {
    data class TestPermission(val userId: UserId, val entity: Any, val permission: GroupPermission)

    private val permissions = mutableSetOf<TestPermission>()

    override suspend fun <T : Any> hasPermission(entity: T, permission: GroupPermission): Boolean {
        val userId = CoroutineUserIdContext.get() ?: return false

        return if (permissions.isEmpty()) {
            // No permissions defined, so allow everything
            true
        } else {
            permissions.contains(TestPermission(userId, entity, permission))
        }
    }

    fun <T : Any> addPermission(userId: UserId, entity: T, permission: GroupPermission) {
        permissions.add(TestPermission(userId, entity, permission))
    }
}
