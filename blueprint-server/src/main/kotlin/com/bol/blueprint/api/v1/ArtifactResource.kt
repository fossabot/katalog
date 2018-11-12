package com.bol.blueprint.api.v1

import com.bol.blueprint.domain.*
import com.bol.blueprint.domain.readmodels.ArtifactReadModel
import com.bol.blueprint.domain.readmodels.NamespaceReadModel
import com.bol.blueprint.domain.readmodels.SchemaReadModel
import com.bol.blueprint.domain.readmodels.VersionReadModel
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
    private val namespaces: NamespaceReadModel,
    private val schemas: SchemaReadModel,
    private val versions: VersionReadModel,
    private val artifacts: ArtifactReadModel
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
            artifacts.getArtifacts(versionIds)
        } ?: artifacts.getArtifacts()

        return artifacts
            .map {
                toResponse(it)
            }
            .sortedBy { it.filename }
            .paginate(pagination, 25)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: ArtifactId): Responses.Artifact {
        val artifact = artifacts.getArtifact(id)
        artifact?.let {
            return toResponse(artifact)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestParam versionId: VersionId, @RequestPart("file") file: FilePart) = GlobalScope.mono {
        val id: ArtifactId = UUID.randomUUID()

        if (artifacts.getArtifacts(listOf(versionId)).any { it.filename == file.filename() }) throw ResponseStatusException(
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
        artifacts.getArtifact(id)?.let {
            handler.deleteArtifact(id)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    private fun toResponse(artifact: Artifact): Responses.Artifact {
        val (namespaceId, schemaId, versionId) = artifacts.getOwner(artifact.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return versions.getVersion(versionId)?.let { version ->
            schemas.getSchema(schemaId)?.let { schema ->
                namespaces.getNamespace(namespaceId)?.let { namespace ->
                    return Responses.Artifact(
                        id = artifact.id,
                        versionId = versionId,
                        filename = artifact.filename,
                        mediaType = artifact.mediaType,
                        repositoryPath = artifact.getRepositoryPath(namespace, schema, version)
                    )
                }
            }
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }
}