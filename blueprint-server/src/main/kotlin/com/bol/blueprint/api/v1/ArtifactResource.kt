package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.*
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
                    toResponse(it.key, it.value)
                }
                .sortedBy { it.filename }
                .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: UUID) =
            query.getArtifact(ArtifactKey(id))?.let {
                ResponseEntity.ok(toResponse(ArtifactKey(id), it))
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

    private fun toResponse(key: ArtifactKey, value: Artifact): Responses.Artifact {
        val versionKey = query.getArtifactVersionOrThrow(key)

        return Responses.Artifact(
                id = key.id,
                versionId = versionKey.id,
                filename = value.filename,
                mediaType = value.mediaType,
                repositoryPath = key.getRepositoryPath(query)
        )
    }
}