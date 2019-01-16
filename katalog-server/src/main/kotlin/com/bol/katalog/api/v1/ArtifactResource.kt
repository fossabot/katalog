package com.bol.katalog.api.v1

import com.bol.katalog.api.*
import com.bol.katalog.features.registry.*
import com.bol.katalog.security.monoWithUserDetails
import com.bol.katalog.users.GroupPermission
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
class ArtifactResource(
    private val registry: RegistryAggregate,
    private val permissionChecker: PermissionChecker
) {
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
        @RequestParam versionIds: List<VersionId>?
    ) = monoWithUserDetails {
        var result: Collection<Artifact> = versionIds?.let {
            registry.read { getArtifacts(versionIds) }
        } ?: registry.read { getArtifacts() }

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
    ) = monoWithUserDetails {
        val artifact = registry.read { getArtifact(id) }
        toResponse(artifact)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestParam versionId: VersionId,
        @RequestPart("file") file: FilePart
    ) = monoWithUserDetails {
        permissionChecker.requireVersion(versionId, GroupPermission.CREATE)
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
    ) = monoWithUserDetails {
        permissionChecker.requireArtifact(id, GroupPermission.DELETE)
        registry.send(DeleteArtifactCommand(id))
    }

    private suspend fun toResponse(artifact: Artifact): Responses.Artifact {
        return registry.read {
            val (namespaceId, schemaId, versionId) = getOwner(artifact.id)

            val version = getVersion(versionId)
            val schema = getSchema(schemaId)
            val namespace = getNamespace(namespaceId)

            Responses.Artifact(
                id = artifact.id,
                versionId = versionId,
                filename = artifact.filename,
                filesize = artifact.filesize,
                mediaType = artifact.mediaType,
                repositoryPath = artifact.getRepositoryPath(namespace, schema, version)
            )
        }
    }
}