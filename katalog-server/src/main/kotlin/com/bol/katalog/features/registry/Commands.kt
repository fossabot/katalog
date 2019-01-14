package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.Command
import com.bol.katalog.security.GroupId

data class CreateNamespaceCommand(val id: NamespaceId, val groupId: GroupId, val name: String) : Command()
data class DeleteNamespaceCommand(val id: NamespaceId) : Command()
data class CreateSchemaCommand(
    val namespaceId: NamespaceId,
    val id: SchemaId,
    val name: String,
    val schemaType: SchemaType
) : Command()

data class DeleteSchemaCommand(val id: SchemaId) : Command()
data class CreateVersionCommand(val schemaId: SchemaId, val id: VersionId, val version: String) : Command()
data class DeleteVersionCommand(val id: VersionId) : Command()
data class CreateArtifactCommand(
    val versionId: VersionId,
    val id: ArtifactId,
    val filename: String,
    val mediaType: MediaType,
    val data: ByteArray
) : Command()

data class DeleteArtifactCommand(val id: ArtifactId) : Command()