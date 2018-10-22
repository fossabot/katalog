package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.SchemaType
import com.bol.blueprint.queries.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/v1/schemas")
class SchemaResource(
        private val handler: CommandHandler,
        private val query: Query
) {
    object Responses {
        data class Schema(val id: UUID, val namespaceId: UUID, val schema: String)
        data class SchemaCreated(val id: UUID)
    }

    object Requests {
        data class NewSchema(val namespaceId: UUID, val schema: String)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?, @RequestParam namespaceIds: List<UUID>?): Page<Responses.Schema> {
        val schemas = namespaceIds?.let {
            query.getSchemas(namespaceIds.map { id -> NamespaceKey(id) })
        } ?: query.getSchemas()

        return schemas
                .map {
                    Responses.Schema(
                            id = it.key.id,
                            namespaceId = getNamespaceOrThrow(it.key),
                            schema = it.value.name
                    )
                }
                .sortedBy { it.schema }
                .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: UUID) =
            query.getSchema(SchemaKey(id))?.let {
                ResponseEntity.ok(Responses.Schema(id = id, namespaceId = getNamespaceOrThrow(SchemaKey(id)), schema = it.name))
            } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody data: Requests.NewSchema): Responses.SchemaCreated {
        val key = SchemaKey(id = UUID.randomUUID())
        val namespaceKey = NamespaceKey(data.namespaceId)

        if (query.getSchemas(listOf(namespaceKey)).values.any { it.name == data.schema }) throw ResponseStatusException(HttpStatus.CONFLICT)

        handler.createSchema(namespaceKey, key, data.schema, SchemaType.default())
        return Responses.SchemaCreated(key.id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: UUID) {
        val key = SchemaKey(id)

        query.getSchema(key)?.let {
            handler.deleteSchema(key)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    fun getNamespaceOrThrow(id: SchemaKey) = query.getSchemaNamespace(id)?.id
            ?: throw RuntimeException("Could not find the namespace belonging to schema: $id")
}