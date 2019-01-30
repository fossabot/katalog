package com.bol.katalog.security

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.Artifact
import com.bol.katalog.features.registry.Namespace
import com.bol.katalog.features.registry.Schema
import com.bol.katalog.features.registry.Version
import com.bol.katalog.users.GroupPermission

class CoroutineContextPermissionManager(
    val security: Aggregate<Security>
) : PermissionManager {
    override suspend fun <T : Any> hasPermission(entity: T, permission: GroupPermission): Boolean {
        return getCurrentUser()?.let { user ->
            return hasPermissionForUser(user, entity, permission)
        } ?: false
    }

    private suspend fun <T : Any> hasPermissionForUser(user: User, entity: T, permission: GroupPermission): Boolean =
        when (entity) {
            is GroupId ->
                security.read {
                    hasPermission(
                        user,
                        entity,
                        permission
                    )
                }
            is Group -> hasPermissionForUser(user, entity.id, permission)
            is Namespace -> hasPermissionForUser(user, entity.groupId, permission)
            is Schema -> hasPermissionForUser(user, entity.namespace.groupId, permission)
            is Version -> hasPermissionForUser(user, entity.schema.namespace.groupId, permission)
            is Artifact -> hasPermissionForUser(user, entity.version.schema.namespace.groupId, permission)
            else -> throw IllegalStateException("Cannot determine permission for entity: $entity")
        }

    private suspend fun getCurrentUser(): User? {
        return CoroutineUserIdContext.get()?.let { userId ->
            security.read { findUserById(userId) }
        }
    }
}