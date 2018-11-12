package com.bol.blueprint.domain.readmodels

import com.bol.blueprint.cqrs.EventHandler
import com.bol.blueprint.cqrs.EventHandlerBuilder.Companion.eventHandler
import com.bol.blueprint.cqrs.Resettable
import com.bol.blueprint.domain.*
import org.springframework.stereotype.Component

@Component
class SchemaReadModel : EventHandler, Resettable {
    data class Entry(val namespaceId: NamespaceId, val schemaId: SchemaId, val schema: Schema)

    private val schemas = mutableMapOf<SchemaId, Entry>()

    private val handler = eventHandler {
        handle<SchemaCreatedEvent> {
            val schema = Schema(it.id, it.name, it.schemaType)
            schemas[it.id] = Entry(it.namespaceId, it.id, schema)
        }
        handle<SchemaDeletedEvent> {
            schemas.remove(it.id)
        }
    }

    override fun getEventHandlerChannel() = handler

    override fun reset() {
        schemas.clear()
    }

    /**
     * Get all available schemas
     */
    fun getSchemas(): Collection<Schema> = schemas.values.map { it.schema }

    /**
     * Get all schemas for the specified namespaces
     */
    fun getSchemas(namespaceIds: Collection<NamespaceId>): Collection<Schema> = schemas.filter {
        namespaceIds.any { id ->
            it.value.namespaceId == id
        }
    }.map { it.value.schema }

    /**
     * Get schema based on id
     */
    fun getSchema(schemaId: SchemaId): Schema? = schemas[schemaId]?.schema

    fun getSchemaNamespaceId(schemaId: SchemaId) = schemas[schemaId]?.namespaceId

    fun findSchema(namespaceId: NamespaceId, schema: String) =
        schemas.values
            .filter {
                it.namespaceId == namespaceId && it.schema.name == schema
            }
            .map { it.schema }
            .singleOrNull()
}