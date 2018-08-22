package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.SchemaType
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.runBlocking
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
        data class Multiple(val schemas: List<Single>)
        data class Single(val name: String)
        data class Detail(val name: String)
    }

    object Requests {
        data class NewSchema(val name: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String): Responses.Multiple {
        return Responses.Multiple(query.getSchemas(NamespaceKey(namespace)).map {
            Responses.Single(it.name)
        })
    }

    @GetMapping("/{name}")
    fun getOne(@PathVariable namespace: String, @PathVariable name: String): Responses.Detail {
        val it = query.getSchema(SchemaKey(namespace, name)) ?: throw ResourceNotFoundException()
        return Responses.Detail(name = it.name)
    }

    @PostMapping
    fun create(@PathVariable namespace: String, @Valid @RequestBody data: Requests.NewSchema): ResponseEntity<Void> {
        runBlocking { handler.createSchema(SchemaKey(namespace = namespace, schema = data.name), SchemaType.default()) }
        return ResponseEntity(HttpStatus.CREATED)
    }
}