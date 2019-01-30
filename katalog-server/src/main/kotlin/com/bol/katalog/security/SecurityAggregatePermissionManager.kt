package com.bol.katalog.security

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.users.GroupPermission

class SecurityAggregatePermissionManager(
    val security: Aggregate<Security>
) : PermissionManager {
    override suspend fun filterPermittedGroups(groupIds: List<GroupId>, permission: GroupPermission): List<GroupId> {
        return getCurrentUser()?.let { user ->
            // System user has all rights on all groups
            if (user == SystemUser.get()) {
                return groupIds
            }

            security.read {
                groupIds.filter {
                    hasPermission(user, it, permission)
                }
            }
        } ?: emptyList()
    }

    private suspend fun getCurrentUser(): User? {
        return CoroutineUserIdContext.get()?.let { userId ->
            security.read { findUserById(userId) }
        }
    }
}