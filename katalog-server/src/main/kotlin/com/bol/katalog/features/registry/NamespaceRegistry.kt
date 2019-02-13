package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.requirePermissionOrForbiddenBy
import com.bol.katalog.users.GroupPermission

class NamespaceRegistry(
    context: AggregateContext,
    private val permissionManager: PermissionManager
) {
    private val namespaces = context.map<NamespaceId, Namespace>("registry/v1/namespaces")

    /**
     * Get all available namespaces
     */
    suspend fun getAll(): Sequence<Namespace> = namespaces.read { values }.filteredForUser().asSequence()

    /**
     * Get namespace based on id
     */
    suspend fun getById(namespaceId: NamespaceId): Namespace {
        val single =
            namespaces.read { this[namespaceId] } ?: throw NotFoundException("Unknown namespace id: $namespaceId")
        permissionManager.requirePermissionOrForbiddenBy(single, GroupPermission.READ)
        return single
    }

    suspend fun getByName(namespace: String): Namespace {
        val filtered = namespaces.read { values }.filteredForUser()
        return filtered.firstOrNull { it.name == namespace }
            ?: throw NotFoundException("Unknown namespace: $namespace")
    }

    suspend fun exists(namespaceId: NamespaceId? = null, namespace: String? = null) =
        namespaces.read { values }.any {
        (namespaceId != null && it.id == namespaceId) || (namespace != null && it.name == namespace)
    }

    suspend fun add(namespace: Namespace) {
        namespaces.write { this[namespace.id] = namespace }
    }

    suspend fun removeById(namespaceId: NamespaceId) {
        namespaces.write { remove(namespaceId) }
    }

    private suspend fun Collection<Namespace>.filteredForUser() =
        filter { permissionManager.hasPermissionBy(it, GroupPermission.READ) }

    fun reset() {
        namespaces.reset()
    }
}