package com.bol.blueprint.domain

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import java.net.URI

interface Sink<T> {
    fun getSink(): SendChannel<T>
}

data class NamespaceCreatedEvent(val key: NamespaceKey) : Event()
data class SchemaCreatedEvent(val key: SchemaKey, val schemaType: SchemaType) : Event()
data class VersionCreatedEvent(val key: VersionKey) : Event()
data class ArtifactCreatedEvent(val key: ArtifactKey, val mediaType: MediaType, val path: URI) : Event()

fun <T> eventHandler(handler: suspend (T) -> Unit): SendChannel<T> {
    return actor {
        for (event in channel) {
            handler(event)
        }
    }
}
