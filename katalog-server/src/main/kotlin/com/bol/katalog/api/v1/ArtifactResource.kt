package com.bol.katalog.api.v1

import com.bol.katalog.domain.*
import com.bol.katalog.domain.aggregates.ArtifactAggregate
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.domain.aggregates.SchemaAggregate
import com.bol.katalog.domain.aggregates.VersionAggregate
import com.bol.katalog.security.KatalogUserDetails
import com.bol.katalog.security.monoWithUserDetails
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/api/v1/artifacts")
@PreAuthorize("hasRole('USER')")
class ArtifactResource(
    private val processor: DomainProcessor,
    private val namespaces: NamespaceAggregate,
    private val schemas: SchemaAggregate,
    private val versions: VersionAggregate,
    private val artifacts: ArtifactAggregate
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
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        pagination: PaginationRequest,
        sorting: SortingRequest,
        @RequestParam versionIds: List<VersionId>?
    ) = monoWithUserDetails(userDetails) {
        var result: Collection<Artifact> = versionIds?.let {
            artifacts.getArtifacts(versionIds)
        } ?: artifacts.getArtifacts()

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
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        @PathVariable id: ArtifactId
    ) = monoWithUserDetails(userDetails) {
        val artifact = artifacts.getArtifact(id)
        toResponse(artifact)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        @RequestParam versionId: VersionId,
        @RequestPart("file") file: FilePart
    ) = monoWithUserDetails(userDetails) {
        val id: ArtifactId = UUID.randomUUID()

        val bytes = file.content().awaitFirst().asInputStream().use {
            val targetArray = ByteArray(it.available())
            it.read(targetArray)
            targetArray
        }
        processor.createArtifact(versionId, id, file.filename(), MediaType.fromFilename(file.filename()), bytes)
        Responses.ArtifactCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal userDetails: KatalogUserDetails,
        @PathVariable id: ArtifactId
    ) = monoWithUserDetails(userDetails) {
        processor.deleteArtifact(id)
    }

    private suspend fun toResponse(artifact: Artifact): Responses.Artifact {
        val (namespaceId, schemaId, versionId) = artifacts.getOwner(artifact.id)

        val version = versions.getVersion(versionId)
        val schema = schemas.getSchema(schemaId)
        val namespace = namespaces.getNamespace(namespaceId)

        return Responses.Artifact(
            id = artifact.id,
            versionId = versionId,
            filename = artifact.filename,
            filesize = artifact.filesize,
            mediaType = artifact.mediaType,
            repositoryPath = artifact.getRepositoryPath(namespace, schema, version)
        )
    }
}