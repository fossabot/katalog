package com.bol.katalog.security

import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.users.GroupPermission

suspend fun PermissionManager.requirePermissionOrForbidden(
    groupId: GroupId,
    permission: GroupPermission
) {
    requirePermission(groupId, permission) {
        val user = CoroutineUserIdContext.get() ?: "[null]"
        throw ForbiddenException("Forbidden: User $user does not have permission $permission on group with id $groupId")
    }
}
