package com.bol.blueprint.domain

import java.net.URI

interface Sink<T> {
    fun getHandler(): suspend (T) -> Unit
}

interface Resettable {
    fun reset()
}

data class NamespaceCreatedEvent(override val metadata: Event.Metadata, val key: NamespaceKey) : Event
data class NamespaceDeletedEvent(override val metadata: Event.Metadata, val key: NamespaceKey) : Event
data class SchemaCreatedEvent(override val metadata: Event.Metadata, val key: SchemaKey, val schemaType: SchemaType) : Event
data class SchemaDeletedEvent(override val metadata: Event.Metadata, val key: SchemaKey) : Event
data class VersionCreatedEvent(override val metadata: Event.Metadata, val key: VersionKey) : Event
data class VersionDeletedEvent(override val metadata: Event.Metadata, val key: VersionKey) : Event
data class ArtifactCreatedEvent(override val metadata: Event.Metadata, val key: ArtifactKey, val mediaType: MediaType, val path: URI, val data: ByteArray) : Event
data class ArtifactDeletedEvent(override val metadata: Event.Metadata, val key: ArtifactKey) : Event
