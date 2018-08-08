package com.bol.blueprint

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import mu.KLogging

class Dispatcher {
    companion object : KLogging()

    private val listeners: MutableList<SendChannel<Event>> = mutableListOf()

    private val sendChannel: SendChannel<Event> = eventHandler { event ->
        logger.debug("Received: $event")
        listeners.forEach {
            launch {
                it.send(event)
            }
        }
    }

    fun addListener(listener: SendChannel<Event>) = listeners.add(listener)

    suspend fun createNamespace(key: NamespaceKey) {
        sendChannel.send(NamespaceCreatedEvent(key))
    }

    suspend fun createSchema(key: SchemaKey, schemaType: SchemaType) {
        sendChannel.send(SchemaCreatedEvent(key, schemaType))
    }

    suspend fun createVersion(key: VersionKey) {
        sendChannel.send(VersionCreatedEvent(key))
    }
}