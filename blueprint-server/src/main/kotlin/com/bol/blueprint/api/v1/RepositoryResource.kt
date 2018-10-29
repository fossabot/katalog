package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.Artifact
import com.bol.blueprint.domain.getBlobStorePath
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.BlobStore
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestController
@RequestMapping("/api/v1/repository/{namespace}/{schema}/{version}")
class RepositoryResource(
        private val query: Query,
        private val blobStore: BlobStore
) {
    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String) = GlobalScope.mono {
        val artifact = query.findArtifact(namespace, schema, version, filename)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        blobStore.get(artifact.id.getBlobStorePath())?.let {
            it
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}

fun Artifact.getRepositoryPath(query: Query): URI {
    val version = query.getArtifactVersionOrThrow(this)
    val schema = query.getVersionSchemaOrThrow(version)
    val namespace = query.getSchemaNamespaceOrThrow(schema)

    val filename = query.getArtifact(id)!!.filename

    return URI.create("/api/v1/repository/${namespace.name}/${schema.name}/${version.semVer.value}/$filename")
}

