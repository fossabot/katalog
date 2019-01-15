package com.bol.katalog.features.registry.aggregates

import com.bol.katalog.cqrs.ConflictException
import com.bol.katalog.cqrs.NotFoundException
import com.bol.katalog.cqrs.Resettable
import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.CommandHandlerBuilder.Companion.handleCommands
import com.bol.katalog.cqrs.events.EventHandler
import com.bol.katalog.cqrs.events.EventHandlerBuilder.Companion.handleEvents
import com.bol.katalog.features.registry.*
import com.bol.katalog.store.BlobStore
import org.springframework.stereotype.Component

@Component
class ArtifactAggregate(
    private val versions: VersionAggregate,
    private val schemas: SchemaAggregate,
    private val blobStore: BlobStore
) : EventHandler, CommandHandler, Resettable {
    data class Entry(
        val namespaceId: NamespaceId,
        val schemaId: SchemaId,
        val versionId: VersionId,
        val artifact: Artifact
    )

    private val artifacts = mutableMapOf<ArtifactId, Entry>()

    override val eventHandler
        get() = handleEvents {
            handle<ArtifactCreatedEvent> {
                val schemaId = versions.getVersionSchemaId(it.versionId)
                val namespaceId = schemas.getSchemaNamespaceId(schemaId)

                val artifact = Artifact(it.id, it.filename, it.data.size, it.mediaType)
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

    override val commandHandler
        get() = handleCommands {
            handle<CreateArtifactCommand> {
                if (artifacts.values.any {
                        it.versionId == command.versionId && it.artifact.filename == command.filename
                    }) throw ConflictException()


                effect {
                    val path = getBlobStorePath(command.id)
                    blobStore.store(path, command.data)
                }

                event(
                    ArtifactCreatedEvent(
                        command.versionId,
                        command.id,
                        command.filename,
                        command.mediaType,
                        command.data
                    )
                )

                complete()
            }

            handle<DeleteArtifactCommand> {
                if (!artifacts.containsKey(command.id)) throw NotFoundException()

                effect {
                    val path = getBlobStorePath(command.id)
                    blobStore.delete(path)
                }

                event(ArtifactDeletedEvent(command.id))
                complete()
            }

            handle<DeleteVersionCommand> {
                artifacts
                    .filterValues { it.versionId == this.command.id }
                    .keys
                    .forEach {
                        require(DeleteArtifactCommand(it))
                    }

                complete()
            }

        }

    override fun reset() {
        artifacts.clear()
    }

    suspend fun getArtifacts() = artifacts.values.map { it.artifact }

    suspend fun getArtifacts(versionIds: Collection<VersionId>) = artifacts.filter {
        versionIds.any { id ->
            it.value.versionId == id
        }
    }.map { it.value.artifact }

    /**
     * Get artifact based on id
     */
    suspend fun getArtifact(artifactId: ArtifactId) =
        artifacts[artifactId]?.artifact ?: throw NotFoundException("Could not find artifact with id: $artifactId")

    suspend fun getArtifactVersionId(artifactId: ArtifactId) = artifacts[artifactId]?.versionId
        ?: throw NotFoundException("Could not find artifact with id: $artifactId")

    suspend fun findArtifact(namespaceId: NamespaceId, schemaId: SchemaId, versionId: VersionId, filename: String) =
        artifacts.values
            .filter {
                it.namespaceId == namespaceId && it.schemaId == schemaId && it.versionId == versionId && it.artifact.filename == filename
            }
            .map { it.artifact }
            .singleOrNull()
            ?: throw NotFoundException("Could not find artifact: $filename in version with id: $versionId in schema with id: $schemaId and namespace with id: $namespaceId")

    suspend fun getOwner(artifactId: ArtifactId) =
        artifacts[artifactId]?.let { Triple(it.namespaceId, it.schemaId, it.versionId) }
            ?: throw NotFoundException("Could not find artifact with id: $artifactId")
}