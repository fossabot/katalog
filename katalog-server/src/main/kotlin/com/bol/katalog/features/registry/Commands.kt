package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.Command
import com.bol.katalog.security.GroupId

data class CreateNamespaceCommand(val id: NamespaceId, val groupId: GroupId, val name: String) : Command
data class DeleteNamespaceCommand(val id: NamespaceId) : Command
data class CreateSchemaCommand(
    val namespaceId: NamespaceId,
    val id: SchemaId,
    val name: String,
    val schemaType: SchemaType
) : Command

data class DeleteSchemaCommand(val id: SchemaId) : Command
data class CreateVersionCommand(val schemaId: SchemaId, val id: VersionId, val version: String) : Command
data class DeleteVersionCommand(val id: VersionId) : Command
data class CreateArtifactCommand(
    val versionId: VersionId,
    val id: ArtifactId,
    val filename: String,
    val mediaType: MediaType,
    val data: ByteArray
) : Command {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateArtifactCommand

        if (versionId != other.versionId) return false
        if (id != other.id) return false
        if (filename != other.filename) return false
        if (mediaType != other.mediaType) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = versionId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

data class DeleteArtifactCommand(val id: ArtifactId) : Command