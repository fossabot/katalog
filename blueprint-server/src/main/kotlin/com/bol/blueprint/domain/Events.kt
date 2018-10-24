package com.bol.blueprint.domain

data class NamespaceCreatedEvent(val id: NamespaceId, val group: GroupId, val name: String)
data class NamespaceDeletedEvent(val id: NamespaceId)
data class SchemaCreatedEvent(val namespaceId: NamespaceId, val id: SchemaId, val name: String, val schemaType: SchemaType)
data class SchemaDeletedEvent(val id: SchemaId)
data class VersionCreatedEvent(val schemaId: SchemaId, val id: VersionId, val version: String)
data class VersionDeletedEvent(val id: VersionId)
data class ArtifactCreatedEvent(val versionId: VersionId, val id: ArtifactId, val filename: String, val mediaType: MediaType, val data: ByteArray)
data class ArtifactDeletedEvent(val id: ArtifactId)
