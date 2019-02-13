package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission
import org.springframework.stereotype.Component

@Component
class SecurityAggregatePermissionManager(
    val security: SecurityAggregate
) : PermissionManager {
    override suspend fun filterPermittedGroups(groupIds: List<GroupId>, permission: GroupPermission): List<GroupId> {
        return getCurrentUser()?.let { user ->
            // System user has all rights on all groups
            if (user == SystemUser.get()) {
                return groupIds
            }

            groupIds.filter {
                security.hasPermission(user.id, it, permission)
            }
        } ?: emptyList()
    }

    private suspend fun getCurrentUser(): User? {
        return CoroutineUserIdContext.get()?.let { userId ->
            security.findUserById(userId)
        }
    }
}