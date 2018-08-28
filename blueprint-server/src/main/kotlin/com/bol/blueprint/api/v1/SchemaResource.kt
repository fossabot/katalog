package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.SchemaType
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.reactor.flux
import kotlinx.coroutines.experimental.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/namespaces/{namespace}/schemas")
class SchemaResource(
    private val handler: CommandHandler,
    private val query: Query
) {
    object Responses {
        data class Single(val name: String)
        data class Detail(val name: String)
    }

    object Requests {
        data class NewSchema(val name: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String) = flux {
        query.getSchemas(NamespaceKey(namespace)).map {
            send(Responses.Single(it.name))
        }
    }

    @GetMapping("/{name}")
    fun getOne(@PathVariable namespace: String, @PathVariable name: String) = mono {
        query.getSchema(SchemaKey(namespace, name))?.let {
            ResponseEntity.ok(Responses.Detail(name = it.name))
        } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    }

    @PostMapping
    fun create(@PathVariable namespace: String, @Valid @RequestBody data: Requests.NewSchema) = mono {
        val key = SchemaKey(namespace, data.name)
        if (query.getSchema(key) == null) {
            handler.createSchema(key, SchemaType.default())
            ResponseEntity.status(HttpStatus.CREATED).build<Void>()
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).build<Void>()
        }
    }

    @DeleteMapping("/{name}")
    fun delete(@PathVariable namespace: String, @PathVariable name: String) = mono {
        val key = SchemaKey(namespace, name)
        query.getSchema(key)?.let {
            handler.deleteSchema(key)
            ResponseEntity.noContent().build<Void>()
        } ?: ResponseEntity.notFound().build<Void>()
    }
}