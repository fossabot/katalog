package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/namespaces", produces = [APPLICATION_BLUEPRINT_V1_VALUE], consumes = [APPLICATION_BLUEPRINT_V1_VALUE])
@Validated
class NamespaceResource(
    private val handler: CommandHandler,
    private val query: Query
) {
    object Responses {
        data class Multiple(val namespaces: List<Single>)
        data class Single(val name: String)
        data class Detail(val name: String)
    }

    object Requests {
        data class NewNamespace(val name: String)
    }

    @GetMapping
    fun get(): Responses.Multiple {
        return Responses.Multiple(query.getNamespaces().map {
            Responses.Single(it.name)
        })
    }

    @GetMapping("/{name}")
    fun getOne(@PathVariable name: String): Responses.Detail {
        val it = query.getNamespace(NamespaceKey(name)) ?: throw ResourceNotFoundException()
        return Responses.Detail(name = it.name)
    }

    @PostMapping
    fun create(@Valid @RequestBody data: Requests.NewNamespace): ResponseEntity<Void> {
        val key = NamespaceKey(namespace = data.name)
        if (query.getNamespace(key) != null) throw ResourceConflictException()
        runBlocking { handler.createNamespace(key) }
        return ResponseEntity(HttpStatus.CREATED)
    }

    @DeleteMapping("/{name}")
    fun delete(@PathVariable name: String): ResponseEntity<Void> {
        val key = NamespaceKey(name)
        query.getNamespace(key) ?: throw ResourceNotFoundException()
        runBlocking { handler.deleteNamespace(key) }
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}