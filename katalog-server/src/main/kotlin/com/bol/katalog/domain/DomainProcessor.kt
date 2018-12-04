package com.bol.katalog.domain

import com.bol.katalog.cqrs.Processor
import org.springframework.stereotype.Component

@Component
class DomainProcessor(
    private val processor: Processor
) {
    suspend fun createNamespace(id: NamespaceId, owner: GroupId, name: String) {
        processor.apply(CreateNamespaceCommand(id, owner, name))
    }

    suspend fun deleteNamespace(id: NamespaceId) {
        processor.apply(DeleteNamespaceCommand(id))
    }

    suspend fun createSchema(namespaceId: NamespaceId, id: SchemaId, name: String, schemaType: SchemaType) {
        processor.apply(CreateSchemaCommand(namespaceId, id, name, schemaType))
    }

    suspend fun deleteSchema(id: SchemaId) {
        processor.apply(DeleteSchemaCommand(id))
    }

    suspend fun createVersion(schemaId: SchemaId, id: VersionId, version: String) {
        processor.apply(CreateVersionCommand(schemaId, id, version))
    }

    suspend fun deleteVersion(id: VersionId) {
        processor.apply(DeleteVersionCommand(id))
    }

    suspend fun createArtifact(
        versionId: VersionId,
        id: ArtifactId,
        filename: String,
        mediaType: MediaType,
        data: ByteArray
    ) {
        processor.apply(CreateArtifactCommand(versionId, id, filename, mediaType, data))
    }

    suspend fun deleteArtifact(id: ArtifactId) {
        processor.apply(DeleteArtifactCommand(id))
    }
}