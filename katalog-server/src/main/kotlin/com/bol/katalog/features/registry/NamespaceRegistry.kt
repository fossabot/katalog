package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.requirePermissionOrForbidden
import com.bol.katalog.users.GroupPermission

class NamespaceRegistry(
    context: AggregateContext,
    private val permissionManager: PermissionManager
) {
    private val namespaces: MutableMap<NamespaceId, Namespace> = context.getMap("registry/v1/namespaces")

    /**
     * Get all available namespaces
     */
    suspend fun getAll(): Collection<Namespace> = namespaces.values.filteredForUser()

    /**
     * Get namespace based on id
     */
    suspend fun getById(namespaceId: NamespaceId): Namespace {
        val single = namespaces[namespaceId] ?: throw NotFoundException("Unknown namespace id: $namespaceId")
        permissionManager.requirePermissionOrForbidden(single.groupId, GroupPermission.READ)
        return single
    }

    suspend fun getByName(namespace: String): Namespace {
        val filtered = namespaces.values.filteredForUser()
        return filtered.firstOrNull { it.name == namespace }
            ?: throw NotFoundException("Unknown namespace: $namespace")
    }

    fun exists(namespaceId: NamespaceId? = null, namespace: String? = null) = namespaces.values.any {
        (namespace != null && it.name == namespace) || (namespaceId != null && it.id == namespaceId)
    }

    fun add(namespace: Namespace) {
        namespaces[namespace.id] = namespace
    }

    fun removeById(namespaceId: NamespaceId) {
        namespaces.remove(namespaceId)
    }

    private suspend fun Collection<Namespace>.filteredForUser() =
        filter { permissionManager.hasPermission(it.groupId, GroupPermission.READ) }
}