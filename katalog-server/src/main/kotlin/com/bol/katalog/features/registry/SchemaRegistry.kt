package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.hazelcast.HazelcastAggregateContext
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class SchemaRegistry(
    private val registry: RegistryAggregate,
    private val context: HazelcastAggregateContext,
    private val permissionManager: PermissionManager
) {
    /**
     * Get all schemas for the specified namespaces
     */
    suspend fun getByNamespaceIds(namespaceIds: Collection<NamespaceId>): Sequence<Schema> {
        return getSchemas().values
            .filteredForUser()
            .asSequence()
            .filter {
                namespaceIds.any { id ->
                    it.namespaceId == id
                }
            }
    }

    suspend fun getByNamespaceId(namespaceId: NamespaceId) = getByNamespaceIds(listOf(namespaceId))

    suspend fun getAll(): Sequence<Schema> {
        return getSchemas().values
            .filteredForUser()
            .asSequence()
    }

    /**
     * Get schema based on id
     */
    suspend fun getById(schemaId: SchemaId): Schema {
        val single = getSchemas()[schemaId] ?: throw NotFoundException("Unknown schema id: $schemaId")
        if (!permissionManager.hasPermissionBy(
                single,
                GroupPermission.READ
            )
        ) throw ForbiddenException("Forbidden to read schema: ${single.name}")
        return single
    }

    suspend fun getByName(namespaceId: NamespaceId, schema: String) =
        getByNamespaceId(namespaceId)
            .singleOrNull { it.name == schema }
            ?: throw NotFoundException("Unknown schema id: $schema in namespace with id: $namespaceId")

    suspend fun exists(namespaceId: NamespaceId, schema: String) = getSchemas().values
        .any {
            it.namespaceId == namespaceId && it.name == schema
        }

    suspend fun add(schema: Schema) {
        getMutableSchemas()[schema.id] = schema
    }

    suspend fun removeById(schemaId: SchemaId) {
        getMutableSchemas().remove(schemaId)
    }

    private suspend fun Collection<Schema>.filteredForUser() =
        filter { permissionManager.hasPermissionBy(it, GroupPermission.READ) }


    private suspend fun getSchemas() = context.map<SchemaId, Schema>("registry/v1/schemas")
    private suspend fun getMutableSchemas() = context.txMap<SchemaId, Schema>("registry/v1/schemas")

    suspend fun reset() {
        getMutableSchemas().destroy()
    }
}