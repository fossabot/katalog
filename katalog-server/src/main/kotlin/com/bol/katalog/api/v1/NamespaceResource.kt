package com.bol.katalog.api.v1

import com.bol.katalog.domain.DomainProcessor
import com.bol.katalog.domain.Group
import com.bol.katalog.domain.Namespace
import com.bol.katalog.domain.NamespaceId
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.security.KatalogUserDetails
import com.bol.katalog.security.monoWithUserDetails
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/v1/namespaces")
@PreAuthorize("hasRole('USER')")
class NamespaceResource(
    private val processor: DomainProcessor,
    private val namespaces: NamespaceAggregate
) {
    object Responses {
        data class Namespace(val id: NamespaceId, val namespace: String, val group: String, val createdOn: Instant)
        data class NamespaceCreated(val id: NamespaceId)
    }

    object Requests {
        data class NewNamespace(val namespace: String, val group: String)
    }

    @GetMapping
    fun get(
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        pagination: PaginationRequest,
        sorting: SortingRequest,
        @RequestParam filter: String?
    ) = monoWithUserDetails(userDetails) {
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
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        @PathVariable id: NamespaceId
    ) = monoWithUserDetails(userDetails) {
        toResponse(namespaces.getNamespace(id))
    }

    private fun toResponse(it: Namespace): Responses.Namespace {
        return Responses.Namespace(
            id = it.id,
            group = it.group.name,
            namespace = it.name,
            createdOn = it.createdOn
        )
    }

    @GetMapping("/find/{namespace}")
    fun findOne(
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        @PathVariable namespace: String
    ) = monoWithUserDetails(userDetails) {
        toResponse(namespaces.findNamespace(namespace))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        @RequestBody data: Requests.NewNamespace
    ) = monoWithUserDetails(userDetails) {
        val id: NamespaceId = UUID.randomUUID()
        processor.createNamespace(id, Group(data.group), data.namespace)
        Responses.NamespaceCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        @PathVariable id: NamespaceId
    ) = monoWithUserDetails(userDetails) {
        processor.deleteNamespace(id)
    }
}