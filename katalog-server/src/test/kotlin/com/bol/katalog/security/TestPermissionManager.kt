package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission
import com.bol.katalog.users.UserId

class TestPermissionManager : PermissionManager {
    data class TestPermission(val userId: UserId, val groupId: GroupId, val permission: GroupPermission)

    private val permissions = mutableSetOf<TestPermission>()

    override suspend fun filterPermittedGroups(groupIds: List<GroupId>, permission: GroupPermission): List<GroupId> {
        val userId = CoroutineUserIdContext.get() ?: return emptyList()

        // System user has all rights on all groups
        if (userId == SystemUser.get().id) {
            return groupIds
        }

        return if (permissions.isEmpty()) {
            // No permissions defined, so allow everything
            groupIds
        } else {
            groupIds.filter {
                permissions.contains(TestPermission(userId, it, permission))
            }
        }
    }

    fun addPermission(userId: UserId, groupId: GroupId, permission: GroupPermission) {
        permissions.add(TestPermission(userId, groupId, permission))
    }
}
