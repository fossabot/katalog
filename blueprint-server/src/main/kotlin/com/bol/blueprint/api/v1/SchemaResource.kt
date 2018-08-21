package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.NamespaceKey
import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.queries.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/namespaces/{namespace}/schemas")
class SchemaResource(private val query: Query) {
    object Responses {
        data class Multiple(val schemas: List<Single>)
        data class Single(val name: String)
        data class Detail(val name: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String): Responses.Multiple {
        return Responses.Multiple(query.getSchemas(NamespaceKey(namespace)).map {
            Responses.Single(it.name)
        })
    }

    @GetMapping("/{name}")
    fun getOne(@PathVariable namespace: String, @PathVariable name: String): Responses.Detail {
        val ns = query.getSchema(SchemaKey(namespace, name))
        if (ns != null) {
            return Responses.Detail(name = ns.name)
        } else {
            throw ResourceNotFoundException()
        }
    }
}