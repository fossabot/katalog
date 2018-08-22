package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.ArtifactKey
import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.MediaType
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.getBlobStorePath
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/namespaces/{namespace}/schemas/{schema}/versions/{version}/artifacts")
class ArtifactResource(
    private val handler: CommandHandler,
    private val query: Query,
    private val blobStore: BlobStore
) {
    object Responses {
        data class Multiple(val artifacts: List<Single>)
        data class Single(val filename: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String): Responses.Multiple {
        return Responses.Multiple(query.getArtifacts(VersionKey(namespace, schema, version)).map {
            Responses.Single(it.filename)
        })
    }

    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String): Resource {
        val bytes = runBlocking { blobStore.get(ArtifactKey(namespace, schema, version, filename).getBlobStorePath()) } ?: throw ResourceNotFoundException()
        return ByteArrayResource(bytes)
    }

    @PostMapping
    fun upload(
        @PathVariable namespace: String,
        @PathVariable schema: String,
        @PathVariable version: String,
        @RequestParam file: MultipartFile
    ) {
        val filename = file.originalFilename ?: throw BadRequestException()

        runBlocking {
            handler.createArtifact(ArtifactKey(namespace, schema, version, filename), MediaType.fromFilename(filename), file.bytes)
        }
    }
}