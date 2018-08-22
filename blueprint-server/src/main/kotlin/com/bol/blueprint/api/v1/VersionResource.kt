package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.runBlocking
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
        data class Multiple(val versions: List<Single>)
        data class Single(val version: String)
        data class Detail(val version: String)
    }

    object Requests {
        data class NewVersion(val version: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String, @PathVariable schema: String): Responses.Multiple {
        return Responses.Multiple(query.getVersions(SchemaKey(namespace, schema)).map {
            Responses.Single(it.version)
        })
    }

    @GetMapping("/{version}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String): Responses.Detail {
        val it = query.getVersion(VersionKey(namespace, schema, version)) ?: throw ResourceNotFoundException()
        return Responses.Detail(version = it.version)
    }

    @PostMapping
    fun create(@PathVariable namespace: String, @PathVariable schema: String, @Valid @RequestBody data: Requests.NewVersion): ResponseEntity<Void> {
        runBlocking { handler.createVersion(VersionKey(namespace = namespace, schema = schema, version = data.version)) }
        return ResponseEntity(HttpStatus.CREATED)
    }
}