package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.queries.Query
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
        data class Namespace(val id: NamespaceKey, val namespace: String)
        data class NamespaceCreated(val id: NamespaceKey)
    }

    object Requests {
        data class NewNamespace(val namespace: String)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?) =
            query
                    .getNamespaces()
                    .map {
                        Responses.Namespace(
                                id = it.id,
                                namespace = it.name
                        )
                    }
                    .sortedBy { it.namespace }
                    .paginate(pagination, 25)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: NamespaceKey) =
            query.getNamespace(id)?.let {
                Responses.Namespace(id = id, namespace = it.name)
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody data: Requests.NewNamespace): Responses.NamespaceCreated {
        val key: NamespaceKey = UUID.randomUUID()
        if (query.getNamespaces().any { it.name == data.namespace }) throw ResponseStatusException(HttpStatus.CONFLICT)
        handler.createNamespace(key, UUID.randomUUID(), data.namespace)
        return Responses.NamespaceCreated(key)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: NamespaceKey) {
        query.getNamespace(id)?.let {
            handler.deleteNamespace(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}