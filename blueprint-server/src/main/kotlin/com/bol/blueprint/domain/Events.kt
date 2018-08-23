package com.bol.blueprint.domain

import java.net.URI

interface Sink {
    fun <T> getHandler(): suspend (Event.Metadata, T) -> Unit
}

interface Resettable {
    fun reset()
}

data class NamespaceCreatedEvent(val key: NamespaceKey)
data class NamespaceDeletedEvent(val key: NamespaceKey)
data class SchemaCreatedEvent(val key: SchemaKey, val schemaType: SchemaType)
data class SchemaDeletedEvent(val key: SchemaKey)
data class VersionCreatedEvent(val key: VersionKey)
data class VersionDeletedEvent(val key: VersionKey)
data class ArtifactCreatedEvent(val key: ArtifactKey, val mediaType: MediaType, val path: URI, val data: ByteArray)
data class ArtifactDeletedEvent(val key: ArtifactKey)
