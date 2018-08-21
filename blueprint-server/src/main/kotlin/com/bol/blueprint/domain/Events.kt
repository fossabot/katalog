package com.bol.blueprint.domain

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import java.net.URI

interface Sink<T> {
    fun getSink(): SendChannel<T>
    fun getSyncHandler(): suspend (T) -> Unit
}

data class NamespaceCreatedEvent(override val metadata: Event.Metadata, val key: NamespaceKey) : Event
data class SchemaCreatedEvent(override val metadata: Event.Metadata, val key: SchemaKey, val schemaType: SchemaType) : Event
data class VersionCreatedEvent(override val metadata: Event.Metadata, val key: VersionKey) : Event
data class ArtifactCreatedEvent(override val metadata: Event.Metadata, val key: ArtifactKey, val mediaType: MediaType, val path: URI) : Event

fun <T> eventHandler(handler: suspend (T) -> Unit): SendChannel<T> {
    return actor {
        for (event in channel) {
            handler(event)
        }
    }
}
