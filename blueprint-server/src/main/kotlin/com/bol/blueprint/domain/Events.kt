package com.bol.blueprint.domain

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor

interface Sink<T> {
    fun getSink(): SendChannel<T>
}

sealed class Event
data class NamespaceCreatedEvent(val key: NamespaceKey) : Event()
data class SchemaCreatedEvent(val key: SchemaKey, val schemaType: SchemaType) : Event()
data class VersionCreatedEvent(val key: VersionKey) : Event()
data class UntypedEvent(val data: Map<String, Any>) : Event()

fun <T> eventHandler(handler: suspend (T) -> Unit): SendChannel<T> {
    return actor {
        for (event in channel) {
            handler(event)
        }
    }
}
