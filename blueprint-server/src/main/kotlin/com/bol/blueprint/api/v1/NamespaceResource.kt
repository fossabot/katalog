package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.GroupKey
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/namespaces")
@Validated
class NamespaceResource(
        private val handler: CommandHandler,
        private val query: Query
) {
    object Responses {
        data class Namespace(val id: UUID, val namespace: String)
        data class NamespaceCreated(val id: UUID)
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
                                id = it.key.id,
                                namespace = it.value.name
                        )
                    }
                    .sortedBy { it.namespace }
                    .paginate(pagination, 25)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: UUID) =
            query.getNamespace(NamespaceKey(id))?.let {
                ResponseEntity.ok(Responses.Namespace(id = id, namespace = it.name))
            } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    @PostMapping
    fun create(@Valid @RequestBody data: Requests.NewNamespace) = GlobalScope.mono {
        val key = NamespaceKey(id = UUID.randomUUID())
        if (query.getNamespace(key) == null) {
            handler.createNamespace(key, GroupKey(UUID.randomUUID()), data.namespace)
            ResponseEntity.status(HttpStatus.CREATED).body(Responses.NamespaceCreated(key.id))
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).build<Void>()
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = GlobalScope.mono {
        val key = NamespaceKey(id)
        query.getNamespace(key)?.let {
            handler.deleteNamespace(key)
            ResponseEntity.noContent().build<Void>()
        } ?: ResponseEntity.notFound().build<Void>()
    }
}