package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.hazelcast.HazelcastAggregateContext
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class SchemaRegistry(
    context: HazelcastAggregateContext,
    private val permissionManager: PermissionManager
) {
    private val schemas = context.map<SchemaId, Schema>("registry/v1/schemas")

    /**
     * Get all schemas for the specified namespaces
     */
    suspend fun getByNamespaceIds(namespaceIds: Collection<NamespaceId>): Sequence<Schema> {
        return schemas.read { values }
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
        return schemas.read { values }
            .filteredForUser()
            .asSequence()
    }

    /**
     * Get schema based on id
     */
    suspend fun getById(schemaId: SchemaId): Schema {
        val single = schemas.read { this[schemaId] } ?: throw NotFoundException("Unknown schema id: $schemaId")
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

    suspend fun exists(namespaceId: NamespaceId, schema: String) = schemas.read { values }
        .any {
            it.namespaceId == namespaceId && it.name == schema
        }

    suspend fun add(schema: Schema) {
        schemas.write { this[schema.id] = schema }
    }

    suspend fun removeById(schemaId: SchemaId) {
        schemas.write { remove(schemaId) }
    }

    private suspend fun Collection<Schema>.filteredForUser() =
        filter { permissionManager.hasPermissionBy(it, GroupPermission.READ) }

    fun reset() {
        schemas.reset()
    }
}