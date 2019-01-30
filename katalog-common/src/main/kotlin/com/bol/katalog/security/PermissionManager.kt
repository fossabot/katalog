package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission

interface PermissionManager {
    suspend fun filterPermittedGroups(groupIds: List<GroupId>, permission: GroupPermission): List<GroupId>
    suspend fun hasPermission(groupId: GroupId, permission: GroupPermission) =
        filterPermittedGroups(listOf(groupId), permission).isNotEmpty()

    suspend fun requirePermission(
        groupId: GroupId,
        permission: GroupPermission,
        throwIfNotAllowed: () -> Throwable
    ) {
        if (!hasPermission(groupId, permission)) {
            throw throwIfNotAllowed()
        }
    }
}