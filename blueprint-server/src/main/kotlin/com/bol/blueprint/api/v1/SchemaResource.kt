package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.*
import com.bol.blueprint.domain.aggregates.NamespaceAggregate
import com.bol.blueprint.domain.aggregates.SchemaAggregate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/schemas")
class SchemaResource(
    private val handler: Processor,
    private val namespaces: NamespaceAggregate,
    private val schemas: SchemaAggregate
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
        toResponse(schemas.getSchema(id))

    private fun toResponse(it: Schema): Responses.Schema {
        val namespaceId = schemas.getSchemaNamespaceId(it.id)
        val namespace = namespaces.getNamespace(namespaceId)

        return Responses.Schema(
            id = it.id,
            namespace = Responses.Schema.Namespace(namespace.id, namespace.name),
            schema = it.name
        )
    }

    @GetMapping("/find/{namespace}/{schema}")
    fun findOne(@PathVariable namespace: String, @PathVariable schema: String): Responses.Schema {
        val ns = namespaces.findNamespace(namespace)
        val s = schemas.findSchema(ns.id, schema)
        return toResponse(s)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody data: Requests.NewSchema) = GlobalScope.mono {
        val id: SchemaId = UUID.randomUUID()
        handler.createSchema(data.namespaceId, id, data.schema, SchemaType.default())
        Responses.SchemaCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: SchemaId) = GlobalScope.mono {
        handler.deleteSchema(id)
    }
}