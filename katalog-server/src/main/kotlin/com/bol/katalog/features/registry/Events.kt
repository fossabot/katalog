package com.bol.katalog.features.registry

import com.bol.katalog.cqrs.Event
import com.bol.katalog.security.GroupId

data class NamespaceCreatedEvent(val id: NamespaceId, val groupId: GroupId, val name: String) : Event()
data class NamespaceDeletedEvent(val id: NamespaceId) : Event()
data class SchemaCreatedEvent(
    val namespaceId: NamespaceId,
    val id: SchemaId,
    val name: String,
    val schemaType: SchemaType
) : Event()

data class SchemaDeletedEvent(val id: SchemaId) : Event()
data class VersionCreatedEvent(val schemaId: SchemaId, val id: VersionId, val version: String) : Event()
data class VersionDeletedEvent(val id: VersionId) : Event()
data class ArtifactCreatedEvent(
    val versionId: VersionId,
    val id: ArtifactId,
    val filename: String,
    val mediaType: MediaType,
    val data: ByteArray
) : Event()

data class ArtifactDeletedEvent(val id: ArtifactId) : Event()
