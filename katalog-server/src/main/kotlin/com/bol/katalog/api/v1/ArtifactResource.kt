package com.bol.katalog.api.v1

import com.bol.katalog.domain.*
import com.bol.katalog.domain.aggregates.ArtifactAggregate
import com.bol.katalog.domain.aggregates.NamespaceAggregate
import com.bol.katalog.domain.aggregates.SchemaAggregate
import com.bol.katalog.domain.aggregates.VersionAggregate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.mono
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
        pagination: PaginationRequest,
        sorting: SortingRequest,
        @RequestParam versionIds: List<VersionId>?
    ) = GlobalScope.mono {
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
    fun getOne(@PathVariable id: ArtifactId) = GlobalScope.mono {
        val artifact = artifacts.getArtifact(id)
        toResponse(artifact)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestParam versionId: VersionId, @RequestPart("file") file: FilePart) = GlobalScope.mono {
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
    fun delete(@PathVariable id: ArtifactId) = GlobalScope.mono {
        processor.deleteArtifact(id)
    }

    private fun toResponse(artifact: Artifact): Responses.Artifact {
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