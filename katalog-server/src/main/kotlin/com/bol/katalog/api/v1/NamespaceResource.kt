package com.bol.katalog.api.v1

import com.bol.katalog.domain.Namespace
import com.bol.katalog.domain.NamespaceId
import com.bol.katalog.domain.Processor
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/namespaces")
class NamespaceResource(
    private val handler: Processor,
    private val namespaces: NamespaceAggregate
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
        namespaces
            .getNamespaces()
            .filter { filter == null || it.name.contains(filter, true) }
            .map { toResponse(it) }
            .sortedBy { it.namespace }
            .paginate(pagination, 25)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: NamespaceId) =
        toResponse(namespaces.getNamespace(id))

    private fun toResponse(it: Namespace): Responses.Namespace {
        return Responses.Namespace(
            id = it.id,
            namespace = it.name
        )
    }

    @GetMapping("/find/{namespace}")
    fun findOne(@PathVariable namespace: String) =
        toResponse(namespaces.findNamespace(namespace))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody data: Requests.NewNamespace) = GlobalScope.mono {
        val id: NamespaceId = UUID.randomUUID()
        handler.createNamespace(id, UUID.randomUUID(), data.namespace)
        Responses.NamespaceCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: NamespaceId) = GlobalScope.mono {
        handler.deleteNamespace(id)
    }
}