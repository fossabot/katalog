package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.users.GroupPermission

class SchemaRegistry(
    context: AggregateContext,
    private val permissionManager: PermissionManager
) {
    private val schemas: MutableMap<SchemaId, Schema> = context.getMap("registry/v1/schemas")

    /**
     * Get all available schemas
     */
    suspend fun getAll(): Collection<Schema> = schemas.values.schemasFilteredForUser()

    /**
     * Get all schemas for the specified namespaces
     */
    suspend fun getByNamespaceIds(namespaceIds: Collection<NamespaceId>): Collection<Schema> = schemas.values
        .schemasFilteredForUser()
        .filter {
            namespaceIds.any { id ->
                it.namespace.id == id
            }
        }

    /**
     * Get schema based on id
     */
    suspend fun getById(schemaId: SchemaId): Schema {
        val single = schemas[schemaId] ?: throw NotFoundException("Unknown schema id: $schemaId")
        if (!permissionManager.hasPermission(
                single.namespace.groupId,
                GroupPermission.READ
            )
        ) throw ForbiddenException("Forbidden to read schema: ${single.name}")
        return single
    }

    suspend fun getByName(namespaceId: NamespaceId, schema: String) =
        schemas.values
            .schemasFilteredForUser().singleOrNull {
                it.namespace.id == namespaceId && it.name == schema
            }
            ?: throw NotFoundException("Unknown schema id: $schema in namespace with id: $namespaceId")

    fun exists(namespaceId: NamespaceId, schema: String) = schemas.values
        .any {
        it.namespace.id == namespaceId && it.name == schema
    }

    fun add(schema: Schema) {
        schemas[schema.id] = schema
    }

    fun removeById(schemaId: SchemaId) {
        schemas.remove(schemaId)
    }

    private suspend fun Collection<Schema>.schemasFilteredForUser() =
        filter { permissionManager.hasPermission(it.namespace.groupId, GroupPermission.READ) }
}