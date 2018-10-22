package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.domain.SchemaKey
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
        data class Schema(val id: SchemaKey, val namespaceId: NamespaceKey, val schema: String)
        data class SchemaCreated(val id: SchemaKey)
    }

    object Requests {
        data class NewSchema(val namespaceId: NamespaceKey, val schema: String)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?, @RequestParam namespaceIds: List<NamespaceKey>?): Page<Responses.Schema> {
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
    fun getOne(@PathVariable id: SchemaKey): Responses.Schema {
        val schema = query.getSchema(id)
        schema?.let {
            return Responses.Schema(id = id, namespaceId = query.getSchemaNamespaceOrThrow(schema).id, schema = it.name)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody data: Requests.NewSchema): Responses.SchemaCreated {
        val key: SchemaKey = UUID.randomUUID()
        val namespaceKey = data.namespaceId

        if (query.getSchemas(listOf(namespaceKey)).any { it.name == data.schema }) throw ResponseStatusException(HttpStatus.CONFLICT)

        handler.createSchema(namespaceKey, key, data.schema, SchemaType.default())
        return Responses.SchemaCreated(key)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: SchemaKey) {
        query.getSchema(id)?.let {
            handler.deleteSchema(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}