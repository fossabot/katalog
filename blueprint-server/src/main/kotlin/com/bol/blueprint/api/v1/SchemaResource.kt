package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.NamespaceId
import com.bol.blueprint.domain.SchemaId
import com.bol.blueprint.domain.SchemaType
import com.bol.blueprint.queries.Query
import org.springframework.http.HttpStatus
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
        data class Schema(val id: SchemaId, val namespaceId: NamespaceId, val schema: String)
        data class SchemaCreated(val id: SchemaId)
    }

    object Requests {
        data class NewSchema(val namespaceId: NamespaceId, val schema: String)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?, @RequestParam namespaceIds: List<NamespaceId>?): Page<Responses.Schema> {
        val schemas = namespaceIds?.let {
            query.getSchemas(namespaceIds)
        } ?: query.getSchemas()

        return schemas
                .map {
                    Responses.Schema(
                            id = it.id,
                            namespaceId = query.getSchemaNamespaceOrThrow(it).id,
                            schema = it.name
                    )
                }
                .sortedBy { it.schema }
                .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: SchemaId): Responses.Schema {
        val schema = query.getSchema(id)
        schema?.let {
            return Responses.Schema(id = id, namespaceId = query.getSchemaNamespaceOrThrow(schema).id, schema = it.name)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody data: Requests.NewSchema): Responses.SchemaCreated {
        val id: SchemaId = UUID.randomUUID()

        if (query.getSchemas(listOf(data.namespaceId)).any { it.name == data.schema }) throw ResponseStatusException(HttpStatus.CONFLICT)

        handler.createSchema(data.namespaceId, id, data.schema, SchemaType.default())
        return Responses.SchemaCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: SchemaId) {
        query.getSchema(id)?.let {
            handler.deleteSchema(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}