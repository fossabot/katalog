package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.ArtifactKey
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/namespaces/{namespace}/schemas/{schema}/versions/{version}/artifacts")
class ArtifactResource(private val query: Query) {
    object Responses {
        data class Multiple(val artifacts: List<Single>)
        data class Single(val filename: String)
        data class Detail(val filename: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String): Responses.Multiple {
        return Responses.Multiple(query.getArtifacts(VersionKey(namespace, schema, version)).map {
            Responses.Single(it.filename)
        })
    }

    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String): Responses.Detail {
        val it = query.getArtifact(ArtifactKey(namespace, schema, version, filename))
                ?: throw ResourceNotFoundException()
        return Responses.Detail(filename = it.filename)
    }
}