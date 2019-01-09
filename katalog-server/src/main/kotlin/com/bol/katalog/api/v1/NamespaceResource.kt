package com.bol.katalog.api.v1

import com.bol.katalog.api.PermissionChecker
import com.bol.katalog.domain.DomainProcessor
import com.bol.katalog.domain.Namespace
import com.bol.katalog.domain.NamespaceId
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.security.GroupId
import com.bol.katalog.security.monoWithUserDetails
import com.bol.katalog.users.GroupPermission
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/namespaces")
@PreAuthorize("hasRole('USER')")
class NamespaceResource(
    private val processor: DomainProcessor,
    private val namespaces: NamespaceAggregate,
    private val permissionChecker: PermissionChecker
) {
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
    ) = monoWithUserDetails {
        var result: Collection<Namespace> = namespaces
            .getNamespaces()
            .filter { filter == null || it.name.contains(filter, true) }

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
    ) = monoWithUserDetails {
        toResponse(namespaces.getNamespace(id))
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
    ) = monoWithUserDetails {
        toResponse(namespaces.findNamespace(namespace))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody data: Requests.NewNamespace
    ) = monoWithUserDetails {
        permissionChecker.require(data.groupId, GroupPermission.CREATE)
        val id: NamespaceId = UUID.randomUUID()
        processor.createNamespace(id, data.groupId, data.namespace)
        Responses.NamespaceCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: NamespaceId
    ) = monoWithUserDetails {
        permissionChecker.requireNamespace(id, GroupPermission.DELETE)
        processor.deleteNamespace(id)
    }
}