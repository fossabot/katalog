package com.bol.katalog.api.v1

import com.bol.katalog.api.PaginationRequest
import com.bol.katalog.api.SortingRequest
import com.bol.katalog.api.paginate
import com.bol.katalog.api.sort
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.read
import com.bol.katalog.cqrs.send
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.monoWithUserId
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/v1/artifacts")
@PreAuthorize("hasRole('USER')")
class ArtifactResource(private val registry: Aggregate<Registry>) {
    object Responses {
        data class Artifact(
            val id: ArtifactId,
            val versionId: VersionId,
            val filename: String,
            val filesize: Int,
            val mediaType: MediaType,
            val repositoryPath: URI
        )

        data class ArtifactCreated(val id: ArtifactId)
    }

    @GetMapping
    fun get(
        pagination: PaginationRequest,
        sorting: SortingRequest,
        @RequestParam versionIds: List<VersionId>
    ) = monoWithUserId {
        var result: Sequence<Artifact> = registry.read { artifacts.getAll(versionIds) }

        result = result.sort(sorting) { column ->
            when (column) {
                "filename" -> {
                    { it.filename }
                }
                "filesize" -> {
                    { it.filesize }
                }
                else -> {
                    { it.filename }
                }
            }
        }

        result
            .paginate(pagination) {
                toResponse(it)
            }
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: ArtifactId
    ) = monoWithUserId {
        val artifact = registry.read { artifacts.getById(id) }
        toResponse(artifact)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestParam versionId: VersionId,
        @RequestPart("file") file: FilePart
    ) = monoWithUserId {
        val id: ArtifactId = UUID.randomUUID().toString()

        val bytes = file.content().awaitFirst().asInputStream().use {
            val targetArray = ByteArray(it.available())
            it.read(targetArray)
            targetArray
        }
        registry.send(
            CreateArtifactCommand(
                versionId,
                id,
                file.filename(),
                MediaType.fromFilename(file.filename()),
                bytes
            )
        )
        Responses.ArtifactCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: ArtifactId
    ) = monoWithUserId {
        registry.send(DeleteArtifactCommand(id))
    }

    private suspend fun toResponse(artifact: Artifact): Responses.Artifact {
        return registry.read {
            Responses.Artifact(
                id = artifact.id,
                versionId = artifact.version.id,
                filename = artifact.filename,
                filesize = artifact.filesize,
                mediaType = artifact.mediaType,
                repositoryPath = artifact.getRepositoryPath()
            )
        }
    }
}