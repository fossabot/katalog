package com.bol.katalog.api.v1

import com.bol.katalog.api.*
import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.monoWithUserDetails
import com.bol.katalog.users.GroupPermission
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/schemas")
@PreAuthorize("hasRole('USER')")
class SchemaResource(
    private val processor: CommandProcessor,
    private val registry: RegistryAggregate,
    private val permissionChecker: PermissionChecker
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
            registry.getSchemas(namespaceIds)
        } ?: registry.getSchemas()

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
        toResponse(registry.getSchema(id))
    }

    private suspend fun toResponse(it: Schema): Responses.Schema {
        val namespaceId = registry.getSchemaNamespaceId(it.id)
        val namespace = registry.getNamespace(namespaceId)

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
        val ns = registry.findNamespace(namespace)
        val s = registry.findSchema(ns.id, schema)
        toResponse(s)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewSchema
    ) = monoWithUserDetails {
        permissionChecker.requireNamespace(data.namespaceId, GroupPermission.CREATE)
        val id: SchemaId = UUID.randomUUID().toString()
        processor.apply(CreateSchemaCommand(data.namespaceId, id, data.schema, SchemaType.default()))
        Responses.SchemaCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: SchemaId
    ) = monoWithUserDetails {
        permissionChecker.requireSchema(id, GroupPermission.DELETE)
        processor.apply(DeleteSchemaCommand(id))
    }
}