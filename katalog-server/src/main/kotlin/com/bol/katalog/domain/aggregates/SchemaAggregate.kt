package com.bol.katalog.domain.aggregates

import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import com.bol.katalog.domain.*
import org.springframework.stereotype.Component

@Component
class SchemaAggregate : EventHandler, CommandHandler, Resettable {
    data class Entry(val namespaceId: NamespaceId, val schemaId: SchemaId, val schema: Schema)

    private val schemas = mutableMapOf<SchemaId, Entry>()

    override val eventHandler
        get() = handleEvents {
            handle<SchemaCreatedEvent> {
                val schema = Schema(it.id, metadata.timestamp, it.name, it.schemaType)
                schemas[it.id] = Entry(it.namespaceId, it.id, schema)
            }
            handle<SchemaDeletedEvent> {
                schemas.remove(it.id)
            }
        }

    override val commandHandler
        get() = handleCommands {
            handle<CreateSchemaCommand> {
                if (schemas.values.any {
                        it.namespaceId == command.namespaceId && it.schema.name == command.name
                    }) throw ConflictException()

                event(
                    SchemaCreatedEvent(
                        command.namespaceId,
                        command.id,
                        command.name,
                        command.schemaType
                    )
                )
                complete()
            }

            handle<DeleteSchemaCommand> {
                if (!schemas.containsKey(command.id)) throw NotFoundException()

                event(SchemaDeletedEvent(command.id))
                complete()
            }

            handle<DeleteNamespaceCommand> {
                schemas
                    .filterValues { it.namespaceId == this.command.id }
                    .keys
                    .forEach {
                        require(DeleteSchemaCommand(it))
                    }

                complete()
            }
        }

    override fun reset() {
        schemas.clear()
    }

    /**
     * Get all available schemas
     */
    suspend fun getSchemas(): Collection<Schema> = schemas.values.map { it.schema }

    /**
     * Get all schemas for the specified namespaces
     */
    suspend fun getSchemas(namespaceIds: Collection<NamespaceId>): Collection<Schema> = schemas.filter {
        namespaceIds.any { id ->
            it.value.namespaceId == id
        }
    }.map { it.value.schema }

    /**
     * Get schema based on id
     */
    suspend fun getSchema(schemaId: SchemaId) =
        schemas[schemaId]?.schema ?: throw NotFoundException("Could not find schema with id: $schemaId")

    suspend fun getSchemaNamespaceId(schemaId: SchemaId) = schemas[schemaId]?.namespaceId
        ?: throw NotFoundException("Could not find schema with id: $schemaId")

    suspend fun findSchema(namespaceId: NamespaceId, schema: String) =
        schemas.values
            .filter {
                it.namespaceId == namespaceId && it.schema.name == schema
            }
            .map { it.schema }
            .singleOrNull()
            ?: throw NotFoundException("Could not find schema: $schema in namespace with id: $namespaceId")
}