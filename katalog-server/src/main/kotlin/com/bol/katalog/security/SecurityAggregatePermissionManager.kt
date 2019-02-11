package com.bol.katalog.security

import com.bol.katalog.users.GroupPermission
import mu.KotlinLogging

class SecurityAggregatePermissionManager(
    val security: SecurityAggregate
) : PermissionManager {
    private val log = KotlinLogging.logger {}

    override suspend fun filterPermittedGroups(groupIds: List<GroupId>, permission: GroupPermission): List<GroupId> {
        return getCurrentUser()?.let { user ->
            // System user has all rights on all groups
            if (user == SystemUser.get()) {
                return groupIds
            }

            groupIds.filter {
                security.hasPermission(user, it, permission)
            }
        } ?: emptyList()
    }

    private suspend fun getCurrentUser(): User? {
        return CoroutineUserIdContext.get()?.let { userId ->
            security.findUserById(userId)
        }
    }
}