package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.GroupKey
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.reactor.flux
import kotlinx.coroutines.experimental.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/namespaces")
@Validated
class NamespaceResource(
    private val handler: CommandHandler,
    private val query: Query
) {
    object Responses {
        data class Summary(val name: String, val schemas: List<String>)
        data class Detail(val name: String)
    }

    object Requests {
        data class NewNamespace(val name: String)
    }

    @GetMapping
    fun get() = GlobalScope.flux {
        query.getNamespaces().map { namespace ->
            send(Responses.Summary(
                    name = namespace.name,
                    schemas = query.getSchemas(NamespaceKey(namespace.name)).asSequence().map { schema -> schema.name }.sorted().toList()
            ))
        }
    }

    @GetMapping("/{name}")
    fun getOne(@PathVariable name: String) = GlobalScope.mono {
        query.getNamespace(NamespaceKey(name))?.let {
            ResponseEntity.ok(Responses.Detail(name = it.name))
        } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @PostMapping
    fun create(@Valid @RequestBody data: Requests.NewNamespace) = GlobalScope.mono {
        val key = NamespaceKey(namespace = data.name)
        if (query.getNamespace(key) == null) {
            handler.createNamespace(key, GroupKey("unknown-group"))
            ResponseEntity.status(HttpStatus.CREATED).build<Void>()
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).build<Void>()
        }
    }

    @DeleteMapping("/{name}")
    fun delete(@PathVariable name: String) = GlobalScope.mono {
        val key = NamespaceKey(name)
        query.getNamespace(key)?.let {
            handler.deleteNamespace(key)
            ResponseEntity.noContent().build<Void>()
        } ?: ResponseEntity.notFound().build<Void>()
    }
}