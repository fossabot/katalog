package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.ArtifactKey
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.getBlobStorePath
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
    suspend fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String): ByteArray {
        val namespaceKey = query.getNamespaceKey(namespace) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val schemaKey = query.getSchemaKey(namespaceKey, schema) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val versionKey = query.getVersionKey(schemaKey, version) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        val artifactKey = query.getArtifactKey(versionKey, filename)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        blobStore.get(artifactKey.getBlobStorePath())?.let {
            return it
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}

fun ArtifactKey.getRepositoryPath(query: Query): URI {
    val versionKey = query.getArtifactVersionOrThrow(this)
    val schemaKey = query.getVersionSchemaOrThrow(versionKey)
    val namespaceKey = query.getSchemaNamespaceOrThrow(schemaKey)

    val version = query.getVersion(versionKey)!!.version
    val schema = query.getSchema(schemaKey)!!.name
    val namespace = query.getNamespace(namespaceKey)!!.name

    val filename = query.getArtifact(this)!!.filename

    return URI.create("/api/v1/repository/$namespace/$schema/$version/$filename")
}

