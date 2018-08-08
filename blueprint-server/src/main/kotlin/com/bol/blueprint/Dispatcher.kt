package com.bol.blueprint

import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired

class Dispatcher(@Autowired val eventStore: EventStore) {
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

    fun addListener(listener: Sink<Event>) = listeners.add(listener.getSink())

    suspend fun createNamespace(key: NamespaceKey) {
        publish(NamespaceCreatedEvent(key))
    }

    suspend fun createSchema(key: SchemaKey, schemaType: SchemaType) {
        publish(SchemaCreatedEvent(key, schemaType))
    }

    suspend fun createVersion(key: VersionKey) {
        publish(VersionCreatedEvent(key))
    }

    private suspend fun publish(event: Event) {
        eventStore.store(event)
        sendChannel.send(event)
    }
}