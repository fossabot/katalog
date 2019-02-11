package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission

interface PermissionManager {
    suspend fun filterPermittedGroups(groupIds: List<GroupId>, permission: GroupPermission): List<GroupId>

    suspend fun filterPermittedGroupsBy(groupIdProviders: List<HasGroupId>, permission: GroupPermission) =
        filterPermittedGroups(groupIdProviders.map { it.groupId }, permission)

    suspend fun hasPermission(groupId: GroupId, permission: GroupPermission) =
        filterPermittedGroups(listOf(groupId), permission).isNotEmpty()

    suspend fun hasPermissionBy(groupIdProviders: HasGroupId, permission: GroupPermission) =
        filterPermittedGroupsBy(listOf(groupIdProviders), permission).isNotEmpty()

    suspend fun requirePermission(
        groupId: GroupId,
        permission: GroupPermission,
        throwIfNotAllowed: suspend () -> Throwable
    ) {
        if (!hasPermission(groupId, permission)) {
            throw throwIfNotAllowed()
        }
    }
}

interface HasGroupId {
    val groupId: GroupId
}