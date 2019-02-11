package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.hazelcast.HazelcastAggregateContext
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.requirePermissionOrForbiddenBy
import com.bol.katalog.users.GroupPermission

class NamespaceRegistry(
    private val context: HazelcastAggregateContext,
    private val permissionManager: PermissionManager
) {
    /**
     * Get all available namespaces
     */
    suspend fun getAll(): Sequence<Namespace> = getNamespaces().values.filteredForUser().asSequence()

    /**
     * Get namespace based on id
     */
    suspend fun getById(namespaceId: NamespaceId): Namespace {
        val single = getNamespaces()[namespaceId] ?: throw NotFoundException("Unknown namespace id: $namespaceId")
        permissionManager.requirePermissionOrForbiddenBy(single, GroupPermission.READ)
        return single
    }

    suspend fun getByName(namespace: String): Namespace {
        val filtered = getNamespaces().values.filteredForUser()
        return filtered.firstOrNull { it.name == namespace }
            ?: throw NotFoundException("Unknown namespace: $namespace")
    }

    suspend fun exists(namespaceId: NamespaceId? = null, namespace: String? = null) = getNamespaces().values.any {
        (namespaceId != null && it.id == namespaceId) || (namespace != null && it.name == namespace)
    }

    suspend fun add(namespace: Namespace) {
        getMutableNamespaces()[namespace.id] = namespace
    }

    suspend fun removeById(namespaceId: NamespaceId) {
        getMutableNamespaces().remove(namespaceId)
    }

    private suspend fun Collection<Namespace>.filteredForUser() =
        filter { permissionManager.hasPermissionBy(it, GroupPermission.READ) }

    private suspend fun getNamespaces() = context.map<NamespaceId, Namespace>("registry/v1/namespaces")
    private suspend fun getMutableNamespaces() = context.txMap<NamespaceId, Namespace>("registry/v1/namespaces")

    suspend fun reset() {
        getMutableNamespaces().destroy()
    }
}