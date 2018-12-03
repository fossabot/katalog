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
    private val handler: Processor,
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
            val mediaType: MediaType,
            val repositoryPath: URI
        )

        data class ArtifactCreated(val id: ArtifactId)
    }

    @GetMapping
    fun get(pagination: PaginationRequest, @RequestParam versionIds: List<VersionId>?) = GlobalScope.mono {
        val artifacts = versionIds?.let {
            artifacts.getArtifacts(versionIds)
        } ?: artifacts.getArtifacts()

        artifacts
            .sortedBy { it.filename }
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
        handler.createArtifact(versionId, id, file.filename(), MediaType.fromFilename(file.filename()), bytes)
        Responses.ArtifactCreated(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: ArtifactId) = GlobalScope.mono {
        handler.deleteArtifact(id)
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
            mediaType = artifact.mediaType,
            repositoryPath = artifact.getRepositoryPath(namespace, schema, version)
        )
    }
}