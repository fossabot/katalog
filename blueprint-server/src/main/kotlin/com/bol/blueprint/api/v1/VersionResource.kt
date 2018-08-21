package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.SchemaKey
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/namespaces/{namespace}/schemas/{schema}/versions")
class VersionResource(private val query: Query) {
    object Responses {
        data class Multiple(val versions: List<Single>)
        data class Single(val version: String)
        data class Detail(val version: String)
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
}