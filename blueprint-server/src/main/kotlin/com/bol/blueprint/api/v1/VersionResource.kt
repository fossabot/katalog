package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.reactor.flux
import kotlinx.coroutines.experimental.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/namespaces/{namespace}/schemas/{schema}/versions")
class VersionResource(
    private val handler: CommandHandler,
    private val query: Query
) {
    object Responses {
        data class Single(val version: String)
        data class Detail(val version: String)
    }

    object Requests {
        data class NewVersion(val version: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String, @PathVariable schema: String) = flux {
        query.getVersions(SchemaKey(namespace, schema)).map {
            send(Responses.Single(it.version))
        }
    }

    @GetMapping("/{version}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String) = mono {
        query.getVersion(VersionKey(namespace, schema, version))?.let {
            ResponseEntity.ok(Responses.Detail(version = it.version))
        } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    }

    @PostMapping
    fun create(@PathVariable namespace: String, @PathVariable schema: String, @Valid @RequestBody data: Requests.NewVersion) = mono {
        val key = VersionKey(namespace = namespace, schema = schema, version = data.version)
        if (query.getVersion(key) == null) {
            handler.createVersion(VersionKey(namespace = namespace, schema = schema, version = data.version))
            ResponseEntity.status(HttpStatus.CREATED).build<Void>()
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).build<Void>()
        }
    }

    @DeleteMapping("/{version}")
    fun delete(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String) = mono {
        val key = VersionKey(namespace, schema, version)
        query.getVersion(key)?.let {
            handler.deleteVersion(key)
            ResponseEntity.noContent().build<Void>()
        } ?: ResponseEntity.notFound().build<Void>()
    }
}