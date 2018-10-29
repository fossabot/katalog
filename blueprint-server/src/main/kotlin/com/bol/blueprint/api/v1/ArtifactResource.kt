package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.*
import com.bol.blueprint.queries.Query
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
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
        data class Artifact(
            val id: ArtifactId,
            val versionId: VersionId,
            val filename: String,
            val mediaType: MediaType,
            val repositoryPath: URI
        )

        data class ArtifactCreated(val id: ArtifactId)
    }

    @GetMapping
    fun get(pagination: PaginationRequest?, @RequestParam versionIds: List<VersionId>?): Page<Responses.Artifact> {
        val artifacts = versionIds?.let {
            query.getArtifacts(versionIds)
        } ?: query.getArtifacts()

        return artifacts
            .map {
                toResponse(it)
            }
            .sortedBy { it.filename }
            .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: ArtifactId): Responses.Artifact {
        val artifact = query.getArtifact(id)
        artifact?.let {
            return toResponse(artifact)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestParam versionId: VersionId, @RequestPart("file") file: FilePart) = GlobalScope.mono {
        val id: ArtifactId = UUID.randomUUID()

        if (query.getArtifacts(listOf(versionId)).any { it.filename == file.filename() }) throw ResponseStatusException(
            HttpStatus.CONFLICT
        )

        val bytes = file.content().awaitFirst().asInputStream().use {
            val targetArray = ByteArray(it.available())
            it.read(targetArray)
            targetArray
        }
        handler.createArtifact(versionId, id, file.filename(), MediaType.fromFilename(file.filename()), bytes)
        Responses.ArtifactCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: ArtifactId) = GlobalScope.mono {
        query.getArtifact(id)?.let {
            handler.deleteArtifact(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    private fun toResponse(artifact: Artifact): Responses.Artifact {
        val version = query.getArtifactVersionOrThrow(artifact)

        return Responses.Artifact(
            id = artifact.id,
            versionId = version.id,
            filename = artifact.filename,
            mediaType = artifact.mediaType,
            repositoryPath = artifact.getRepositoryPath(query)
        )
    }
}