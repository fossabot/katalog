package com.bol.blueprint.domain

import com.bol.blueprint.cqrs.CommandPublisher
import com.bol.blueprint.store.BlobStore
import org.springframework.stereotype.Component

@Component
class CommandHandler(
    val publisher: CommandPublisher,
    private val blobStore: BlobStore
) {
    suspend fun createNamespace(id: NamespaceId, owner: GroupId, name: String) {
        publisher.publish(NamespaceCreatedEvent(id, owner, name))
    }

    suspend fun deleteNamespace(id: NamespaceId) {
        publisher.publish(NamespaceDeletedEvent(id))
    }

    suspend fun createSchema(namespaceId: NamespaceId, id: SchemaId, name: String, schemaType: SchemaType) {
        publisher.publish(
            SchemaCreatedEvent(
                namespaceId,
                id,
                name,
                schemaType
            )
        )
    }

    suspend fun deleteSchema(id: SchemaId) {
        publisher.publish(SchemaDeletedEvent(id))
    }

    suspend fun createVersion(schemaId: SchemaId, id: VersionId, version: String) {
        publisher.publish(VersionCreatedEvent(schemaId, id, version))
    }

    suspend fun deleteVersion(id: VersionId) {
        publisher.publish(VersionDeletedEvent(id))
    }

    suspend fun createArtifact(
        versionId: VersionId,
        id: ArtifactId,
        filename: String,
        mediaType: MediaType,
        data: ByteArray
    ) {
        val path = id.getBlobStorePath()
        blobStore.store(path, data)
        publisher.publish(
            ArtifactCreatedEvent(
                versionId,
                id,
                filename,
                mediaType,
                data
            )
        )
    }

    suspend fun deleteArtifact(id: ArtifactId) {
        val path = id.getBlobStorePath()
        blobStore.delete(path)
        publisher.publish(ArtifactDeletedEvent(id))
    }

    fun reset() = publisher.reset()
}

