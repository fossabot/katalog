package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.ArtifactKey
import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.MediaType
import com.bol.blueprint.domain.VersionKey
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.experimental.reactive.awaitFirst
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/v1/artifacts")
class ArtifactResource(
        private val handler: CommandHandler,
        private val query: Query
) {
    object Responses {
        data class Artifact(val id: UUID, val versionId: UUID, val filename: String, val mediaType: MediaType, val repositoryPath: URI)
        data class ArtifactCreated(val id: UUID)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?, @RequestParam versionIds: List<UUID>?): Page<Responses.Artifact> {
        val artifacts = versionIds?.let {
            query.getArtifacts(versionIds.map { id -> VersionKey(id) })
        } ?: query.getArtifacts()

        return artifacts
                .map {
                    Responses.Artifact(
                            id = it.key.id,
                            versionId = getVersionOrThrow(it.key),
                            filename = it.value.filename,
                            mediaType = it.value.mediaType,
                            repositoryPath = URI.create("todo")
                    )
                }
                .sortedBy { it.filename }
                .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: UUID) =
            query.getArtifact(ArtifactKey(id))?.let {
                ResponseEntity.ok(Responses.Artifact(id = id, versionId = getVersionOrThrow(ArtifactKey(id)), filename = it.filename, mediaType = it.mediaType, repositoryPath = URI.create("todo")))
            } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestParam versionId: UUID, @RequestPart("file") file: FilePart): ArtifactResource.Responses.ArtifactCreated {
        val key = ArtifactKey(id = UUID.randomUUID())
        val versionKey = VersionKey(versionId)

        if (query.getArtifacts(listOf(versionKey)).values.any { it.filename == file.filename() }) throw ResponseStatusException(HttpStatus.CONFLICT)

        val bytes = file.content().awaitFirst().asInputStream().use {
            val targetArray = ByteArray(it.available())
            it.read(targetArray)
            targetArray
        }
        handler.createArtifact(versionKey, key, file.filename(), MediaType.fromFilename(file.filename()), bytes)
        return Responses.ArtifactCreated(key.id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: UUID) {
        val key = ArtifactKey(id)

        query.getArtifact(key)?.let {
            handler.deleteArtifact(key)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    fun getVersionOrThrow(id: ArtifactKey) = query.getArtifactVersion(id)?.id
            ?: throw RuntimeException("Could not find the version belonging to artifact: $id")
}