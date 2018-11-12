package com.bol.blueprint.domain.readmodels

import com.bol.blueprint.cqrs.Resettable
import com.bol.blueprint.cqrs.api.EventHandler
import com.bol.blueprint.cqrs.api.EventHandlerBuilder.Companion.eventHandler
import com.bol.blueprint.domain.*
import org.springframework.stereotype.Component

@Component
class ArtifactReadModel(
    val versions: VersionReadModel,
    val schemas: SchemaReadModel
) : EventHandler, Resettable {
    data class Entry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val artifact: Artifact
    )

    private val artifacts = mutableMapOf<ArtifactId, Entry>()

    override val eventHandler
        get() = eventHandler {
            handle<ArtifactCreatedEvent> {
                val schemaId = versions.getVersionSchemaId(it.versionId)!!
                val namespaceId = schemas.getSchemaNamespaceId(schemaId)!!

                val artifact = Artifact(it.id, it.filename, it.mediaType)
                artifacts[it.id] = Entry(
                    namespaceId,
                    schemaId,
                    it.versionId,
                    artifact
                )
            }
            handle<ArtifactDeletedEvent> {
                artifacts.remove(it.id)
            }
        }

    override fun reset() {
        artifacts.clear()
    }

    fun getArtifacts() = artifacts.values.map { it.artifact }

    fun getArtifacts(versionIds: Collection<VersionId>) = artifacts.filter {
        versionIds.any { id ->
            it.value.versionId == id
        }
    }.map { it.value.artifact }

    /**
     * Get artifact based on id
     */
    fun getArtifact(artifactId: ArtifactId) = artifacts[artifactId]?.artifact

    fun getArtifactVersionId(artifactId: ArtifactId) = artifacts[artifactId]?.versionId

    fun findArtifact(namespaceId: NamespaceId, schemaId: SchemaId, versionId: VersionId, filename: String) =
        artifacts.values
            .filter {
                it.namespaceId == namespaceId && it.schemaId == schemaId && it.versionId == versionId && it.artifact.filename == filename
            }
            .map { it.artifact }
            .singleOrNull()

    fun getOwner(artifactId: ArtifactId) =
        artifacts[artifactId]?.let { Triple(it.namespaceId, it.schemaId, it.versionId) }
}