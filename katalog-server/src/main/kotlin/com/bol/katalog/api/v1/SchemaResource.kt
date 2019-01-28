package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.PermissionManager
import com.bol.katalog.security.monoWithUserDetails
import com.bol.katalog.users.GroupPermission
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/schemas")
@PreAuthorize("hasRole('USER')")
class SchemaResource(
    private val registry: Aggregate<RegistryState>,
    private val permissionManager: PermissionManager
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
            registry.read { getSchemas(namespaceIds) }
        } ?: registry.read { getSchemas() }

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
        toResponse(registry.read { getSchema(id) })
    }

    private suspend fun toResponse(it: Schema): Responses.Schema {
        return registry.read {
            Responses.Schema(
                id = it.id,
                createdOn = it.createdOn,
                namespace = Responses.Schema.Namespace(it.namespace.id, it.namespace.name),
                schema = it.name
            )
        }
    }

    @GetMapping("/find/{namespace}/{schema}")
    fun findOne(
        @PathVariable namespace: String,
        @PathVariable schema: String
    ) = monoWithUserDetails {
        val s = registry.read {
            val ns = findNamespace(namespace)
            findSchema(ns.id, schema)
        }

        toResponse(s)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewSchema
    ) = monoWithUserDetails {
        val id: SchemaId = UUID.randomUUID().toString()
        registry.send(CreateSchemaCommand(data.namespaceId, id, data.schema, SchemaType.default()))
        Responses.SchemaCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: SchemaId
    ) = monoWithUserDetails {
        val schema = registry.read { getSchema(id) }
        permissionManager.requirePermission(schema.namespace.groupId, GroupPermission.DELETE) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }

        registry.send(DeleteSchemaCommand(id))
    }
}