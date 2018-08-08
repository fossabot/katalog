package com.bol.blueprint

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor

interface Sink<T : EventBase> {
    fun getSink(): SendChannel<T>
}

interface EventBase
sealed class Event : EventBase
data class NamespaceCreatedEvent(val key: NamespaceKey) : Event()
data class SchemaCreatedEvent(val key: SchemaKey, val schemaType: SchemaType) : Event()
data class VersionCreatedEvent(val key: VersionKey) : Event()

fun <T> eventHandler(handler: suspend (T) -> Unit): SendChannel<T> {
    return actor {
        for (event in channel) {
            handler(event)
        }
    }
}
