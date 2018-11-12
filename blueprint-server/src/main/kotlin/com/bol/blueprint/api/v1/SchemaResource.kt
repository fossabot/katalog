package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.*
import com.bol.blueprint.domain.readmodels.NamespaceReadModel
import com.bol.blueprint.domain.readmodels.SchemaReadModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/v1/schemas")
class SchemaResource(
    private val handler: CommandHandler,
    private val namespaces: NamespaceReadModel,
    private val schemas: SchemaReadModel
) {
    object Responses {
        data class Schema(val id: SchemaId, val namespace: Namespace, val schema: String) {
            data class Namespace(val id: NamespaceId, val namespace: String)
        }

        data class SchemaCreated(val id: SchemaId)
    }

    object Requests {
        data class NewSchema(val namespaceId: NamespaceId, val schema: String)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?, @RequestParam namespaceIds: List<NamespaceId>?): Page<Responses.Schema> {
        val schemas = namespaceIds?.let {
            schemas.getSchemas(namespaceIds)
        } ?: schemas.getSchemas()

        return schemas
            .map { toResponse(it) }
            .sortedBy { it.schema }
            .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: SchemaId) =
        schemas.getSchema(id)?.let { toResponse(it) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    private fun toResponse(it: Schema): Responses.Schema {
        val namespaceId = schemas.getSchemaNamespaceId(it.id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val namespace = namespaces.getNamespace(namespaceId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return Responses.Schema(
            id = it.id,
            namespace = Responses.Schema.Namespace(namespace.id, namespace.name),
            schema = it.name
        )
    }

    @GetMapping("/find/{namespace}/{schema}")
    fun findOne(@PathVariable namespace: String, @PathVariable schema: String): Responses.Schema {
        return namespaces.findNamespace(namespace)?.let { ns ->
            schemas.findSchema(ns.id, schema)?.let { toResponse(it) }
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody data: Requests.NewSchema) = GlobalScope.mono {
        val id: SchemaId = UUID.randomUUID()

        if (schemas.getSchemas(listOf(data.namespaceId)).any { it.name == data.schema }) throw ResponseStatusException(
            HttpStatus.CONFLICT
        )

        handler.createSchema(data.namespaceId, id, data.schema, SchemaType.default())
        Responses.SchemaCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: SchemaId) = GlobalScope.mono {
        schemas.getSchema(id)?.let {
            handler.deleteSchema(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}