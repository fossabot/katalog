package com.bol.katalog.api.v1

import com.bol.katalog.domain.*
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.domain.aggregates.SchemaAggregate
import com.bol.katalog.security.monoWithUserDetails
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/schemas")
@PreAuthorize("hasRole('USER')")
class SchemaResource(
    private val processor: DomainProcessor,
    private val namespaces: NamespaceAggregate,
    private val schemas: SchemaAggregate
) {
    object Responses {
        data class Schema(
            val id: SchemaId,
            val createdOn: Instant,
            val namespace: Namespace,
            val schema: String
        ) {
            data class Namespace(val id: NamespaceId, val namespace: String)
        }

        data class SchemaCreated(val id: SchemaId)
    }

    object Requests {
        data class NewSchema(val namespaceId: NamespaceId, val schema: String)
    }

    @GetMapping
    fun get(
        pagination: PaginationRequest,
        sorting: SortingRequest,
        @RequestParam namespaceIds: List<NamespaceId>?
    ) = monoWithUserDetails {
        var result = namespaceIds?.let {
            schemas.getSchemas(namespaceIds)
        } ?: schemas.getSchemas()

        result = result.sort(sorting) { column ->
            when (column) {
                "schema" -> {
                    { it.name }
                }
                "createdOn" -> {
                    { it.createdOn }
                }
                else -> {
                    { it.name }
                }
            }
        }

        result
            .paginate(pagination) {
                toResponse(it)
            }
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: SchemaId
    ) = monoWithUserDetails {
        toResponse(schemas.getSchema(id))
    }

    private suspend fun toResponse(it: Schema): Responses.Schema {
        val namespaceId = schemas.getSchemaNamespaceId(it.id)
        val namespace = namespaces.getNamespace(namespaceId)

        return Responses.Schema(
            id = it.id,
            createdOn = it.createdOn,
            namespace = Responses.Schema.Namespace(namespace.id, namespace.name),
            schema = it.name
        )
    }

    @GetMapping("/find/{namespace}/{schema}")
    fun findOne(
        @PathVariable namespace: String,
        @PathVariable schema: String
    ) = monoWithUserDetails {
        val ns = namespaces.findNamespace(namespace)
        val s = schemas.findSchema(ns.id, schema)
        toResponse(s)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewSchema
    ) = monoWithUserDetails {
        val id: SchemaId = UUID.randomUUID()
        processor.createSchema(data.namespaceId, id, data.schema, SchemaType.default())
        Responses.SchemaCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: SchemaId
    ) = monoWithUserDetails {
        processor.deleteSchema(id)
    }
}