package com.bol.blueprint.domain

import java.net.URI

data class NamespaceCreatedEvent(val key: NamespaceKey, val group: GroupKey, val name: String)
data class NamespaceDeletedEvent(val key: NamespaceKey)
data class SchemaCreatedEvent(val namespace: NamespaceKey, val key: SchemaKey, val name: String, val schemaType: SchemaType)
data class SchemaDeletedEvent(val key: SchemaKey)
data class VersionCreatedEvent(val schema: SchemaKey, val key: VersionKey, val version: String)
data class VersionDeletedEvent(val key: VersionKey)
data class ArtifactCreatedEvent(val version: VersionKey, val key: ArtifactKey, val filename: String, val mediaType: MediaType, val path: URI, val data: ByteArray)
data class ArtifactDeletedEvent(val key: ArtifactKey)
