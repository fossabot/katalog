package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.Namespace
import com.bol.blueprint.domain.NamespaceId
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/api/v1/namespaces")
class NamespaceResource(
    private val handler: CommandHandler,
    private val query: Query
) {
    object Responses {
        data class Namespace(val id: NamespaceId, val namespace: String)
        data class NamespaceCreated(val id: NamespaceId)
    }

    object Requests {
        data class NewNamespace(val namespace: String)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?, @RequestParam filter: String?) =
        query
            .getNamespaces()
            .filter { filter == null || it.name.contains(filter, true) }
            .map { toResponse(it) }
            .sortedBy { it.namespace }
            .paginate(pagination, 25)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: NamespaceId) =
        query.getNamespace(id)?.let { toResponse(it) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    private fun toResponse(it: Namespace): Responses.Namespace {
        return Responses.Namespace(
            id = it.id,
            namespace = it.name
        )
    }

    @GetMapping("/find/{namespace}")
    fun findOne(@PathVariable namespace: String) = query.findNamespace(namespace)?.let { toResponse(it) } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody data: Requests.NewNamespace) = GlobalScope.mono {
        val id: NamespaceId = UUID.randomUUID()
        if (query.getNamespaces().any { it.name == data.namespace }) throw ResponseStatusException(HttpStatus.CONFLICT)
        handler.createNamespace(id, UUID.randomUUID(), data.namespace)
        Responses.NamespaceCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: NamespaceId) = GlobalScope.mono {
        query.getNamespace(id)?.let {
            handler.deleteNamespace(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}