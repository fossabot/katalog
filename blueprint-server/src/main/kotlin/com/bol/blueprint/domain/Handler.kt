package com.bol.blueprint.domain

import com.bol.blueprint.cqrs.CommandValidator
import com.bol.blueprint.cqrs.EventPublisher
import com.bol.blueprint.store.BlobStore
import org.springframework.stereotype.Component

@Component
class Handler(
    val validator: CommandValidator,
    val publisher: EventPublisher,
    private val blobStore: BlobStore
) {
    suspend fun createNamespace(id: NamespaceId, owner: GroupId, name: String) {
        validator.validate(CreateNamespaceCommand(id, owner, name))
        publisher.publish(NamespaceCreatedEvent(id, owner, name))
    }

    suspend fun deleteNamespace(id: NamespaceId) {
        validator.validate(DeleteNamespaceCommand(id))
        publisher.publish(NamespaceDeletedEvent(id))
    }

    suspend fun createSchema(namespaceId: NamespaceId, id: SchemaId, name: String, schemaType: SchemaType) {
        validator.validate(CreateSchemaCommand(namespaceId, id, name, schemaType))
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
        validator.validate(DeleteSchemaCommand(id))
        publisher.publish(SchemaDeletedEvent(id))
    }

    suspend fun createVersion(schemaId: SchemaId, id: VersionId, version: String) {
        validator.validate(CreateVersionCommand(schemaId, id, version))
        publisher.publish(VersionCreatedEvent(schemaId, id, version))
    }

    suspend fun deleteVersion(id: VersionId) {
        validator.validate(DeleteVersionCommand(id))
        publisher.publish(VersionDeletedEvent(id))
    }

    suspend fun createArtifact(
        versionId: VersionId,
        id: ArtifactId,
        filename: String,
        mediaType: MediaType,
        data: ByteArray
    ) {
        validator.validate(CreateArtifactCommand(versionId, id, filename, mediaType, data))

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
        validator.validate(DeleteArtifactCommand(id))

        val path = id.getBlobStorePath()
        blobStore.delete(path)
        publisher.publish(ArtifactDeletedEvent(id))
    }

    fun reset() = publisher.reset()
}

