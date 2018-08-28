package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.ArtifactKey
import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.MediaType
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.getBlobStorePath
import kotlinx.coroutines.experimental.reactive.awaitFirst
import kotlinx.coroutines.experimental.reactor.flux
import kotlinx.coroutines.experimental.reactor.mono
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/namespaces/{namespace}/schemas/{schema}/versions/{version}/artifacts")
class ArtifactResource(
    private val handler: CommandHandler,
    private val query: Query,
    private val blobStore: BlobStore
) {
    private val log = KotlinLogging.logger {}

    object Responses {
        data class Single(val filename: String)
    }

    @GetMapping
    fun get(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String) = flux {
        query.getArtifacts(VersionKey(namespace, schema, version)).map {
            send(Responses.Single(it.filename))
        }
    }

    @GetMapping("/{filename}")
    fun getOne(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String) = mono {
        blobStore.get(ArtifactKey(namespace, schema, version, filename).getBlobStorePath())?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @PostMapping
    fun upload(
        @PathVariable namespace: String,
        @PathVariable schema: String,
        @PathVariable version: String,
        @RequestPart("file") fileMono: Mono<FilePart>
    ) = mono {
        val file = fileMono.awaitFirst()
        val key = ArtifactKey(namespace, schema, version, file.filename())
        if (query.getArtifact(key) == null) {
            val bytes = file.content().awaitFirst().asInputStream().use {
                val targetArray = ByteArray(it.available())
                it.read(targetArray)
                targetArray
            }
            handler.createArtifact(key, MediaType.fromFilename(file.filename()), bytes)
            ResponseEntity.status(HttpStatus.CREATED).build<Void>()
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).build<Void>()
        }
    }

    @DeleteMapping("/{filename}")
    fun delete(@PathVariable namespace: String, @PathVariable schema: String, @PathVariable version: String, @PathVariable filename: String) = mono {
        val key = ArtifactKey(namespace, schema, version, filename)
        query.getArtifact(key)?.let {
            handler.deleteArtifact(key)
            ResponseEntity.noContent().build<Void>()
        } ?: ResponseEntity.notFound().build<Void>()
    }
}