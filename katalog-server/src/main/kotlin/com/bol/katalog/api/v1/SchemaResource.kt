package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.send
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.monoWithUserId
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/schemas")
@PreAuthorize("hasRole('USER')")
class SchemaResource(private val context: AggregateContext, private val registry: RegistryAggregate) {
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
    ) = monoWithUserId {
        var result = namespaceIds?.let {
            registry.schemas.getByNamespaceIds(namespaceIds)
        } ?: registry.schemas.getAll()

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
    ) = monoWithUserId {
        toResponse(registry.schemas.getById(id))
    }

    private suspend fun toResponse(it: Schema): Responses.Schema {
        val namespace = registry.namespaces.getById(it.namespaceId)
        return Responses.Schema(
            id = it.id,
            createdOn = it.createdOn,
            namespace = Responses.Schema.Namespace(it.namespaceId, namespace.name),
            schema = it.name
        )
    }

    @GetMapping("/find/{namespace}/{schema}")
    fun findOne(
        @PathVariable namespace: String,
        @PathVariable schema: String
    ) = monoWithUserId {
        val ns = registry.namespaces.getByName(namespace)
        val s = registry.schemas.getByName(ns.id, schema)

        toResponse(s)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewSchema
    ) = monoWithUserId {
        val id: SchemaId = UUID.randomUUID().toString()
        context.send(CreateSchemaCommand(data.namespaceId, id, data.schema, SchemaType.default()))
        Responses.SchemaCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: SchemaId
    ) = monoWithUserId {
        context.send(DeleteSchemaCommand(id))
    }
}