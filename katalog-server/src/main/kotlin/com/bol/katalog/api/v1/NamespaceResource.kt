package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.read
import com.bol.katalog.cqrs.send
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.monoWithUserId
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/namespaces")
@PreAuthorize("hasRole('USER')")
class NamespaceResource(private val registry: Aggregate<Registry>) {
    object Responses {
        data class Namespace(val id: NamespaceId, val namespace: String, val groupId: GroupId, val createdOn: Instant)
        data class NamespaceCreated(val id: NamespaceId)
    }

    object Requests {
        data class NewNamespace(val namespace: String, val groupId: GroupId)
    }

    @GetMapping
    fun get(
        pagination: PaginationRequest,
        sorting: SortingRequest,
        @RequestParam filter: String?
    ) = monoWithUserId {
        var result: Sequence<Namespace> = registry
            .read {
                namespaces.getAll().filter { filter == null || it.name.contains(filter, true) }
            }

        result = result.sort(sorting) { column ->
            when (column) {
                "namespace" -> {
                    { it.name }
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
        @PathVariable id: NamespaceId
    ) = monoWithUserId {
        toResponse(registry.read { namespaces.getById(id) })
    }

    private fun toResponse(it: Namespace): Responses.Namespace {
        return Responses.Namespace(
            id = it.id,
            groupId = it.groupId,
            namespace = it.name,
            createdOn = it.createdOn
        )
    }

    @GetMapping("/find/{namespace}")
    fun findOne(
        @PathVariable namespace: String
    ) = monoWithUserId {
        toResponse(registry.read { namespaces.getByName(namespace) })
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewNamespace
    ) = monoWithUserId {
        val id: NamespaceId = UUID.randomUUID().toString()
        registry.send(CreateNamespaceCommand(id, data.groupId, data.namespace))
        Responses.NamespaceCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: NamespaceId
    ) = monoWithUserId {
        registry.send(DeleteNamespaceCommand(id))
    }
}