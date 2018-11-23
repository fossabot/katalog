package com.bol.katalog.domain

import com.bol.katalog.cqrs.CommandProcessor
import com.bol.katalog.store.BlobStore
import org.springframework.stereotype.Component

@Component
class Processor(
    private val processor: CommandProcessor,
    private val blobStore: BlobStore
) {
    suspend fun createNamespace(id: NamespaceId, owner: GroupId, name: String) {
        processor.ifValid(CreateNamespaceCommand(id, owner, name)) {
            event(NamespaceCreatedEvent(id, owner, name))
        }
    }

    suspend fun deleteNamespace(id: NamespaceId) {
        processor.ifValid(DeleteNamespaceCommand(id)) {
            event(NamespaceDeletedEvent(id))
        }
    }

    suspend fun createSchema(namespaceId: NamespaceId, id: SchemaId, name: String, schemaType: SchemaType) {
        processor.ifValid(CreateSchemaCommand(namespaceId, id, name, schemaType)) {
            event(
                SchemaCreatedEvent(
                    namespaceId,
                    id,
                    name,
                    schemaType
                )
            )
        }
    }

    suspend fun deleteSchema(id: SchemaId) {
        processor.ifValid(DeleteSchemaCommand(id)) {
            event(SchemaDeletedEvent(id))
        }
    }

    suspend fun createVersion(schemaId: SchemaId, id: VersionId, version: String) {
        processor.ifValid(CreateVersionCommand(schemaId, id, version)) {
            event(VersionCreatedEvent(schemaId, id, version))
        }
    }

    suspend fun deleteVersion(id: VersionId) {
        processor.ifValid(DeleteVersionCommand(id)) {
            event(VersionDeletedEvent(id))
        }
    }

    suspend fun createArtifact(
        versionId: VersionId,
        id: ArtifactId,
        filename: String,
        mediaType: MediaType,
        data: ByteArray
    ) {
        processor.ifValid(CreateArtifactCommand(versionId, id, filename, mediaType, data)) {
            val path = id.getBlobStorePath()
            blobStore.store(path, data)

            event(
                ArtifactCreatedEvent(
                    versionId,
                    id,
                    filename,
                    mediaType,
                    data
                )
            )
        }
    }

    suspend fun deleteArtifact(id: ArtifactId) {
        processor.ifValid(DeleteArtifactCommand(id)) {
            val path = id.getBlobStorePath()
            blobStore.delete(path)
            event(ArtifactDeletedEvent(id))
        }
    }
}

