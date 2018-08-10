package com.bol.blueprint.domain

import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.getBlobStorePath
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType

class Dispatcher(
    @Autowired val eventStore: EventStore,
    @Autowired val blobStore: BlobStore
) {
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

    suspend fun createArtifact(key: ArtifactKey, mediaType: MediaType, data: ByteArray) {
        val path = key.getBlobStorePath()
        blobStore.store(path, data)
        publish(ArtifactCreatedEvent(key, mediaType, path))
    }

    private suspend fun publish(event: Event) {
        eventStore.store(event)
        sendChannel.send(event)
    }

    suspend fun replayFromStore() {
        var done = false
        var eventQuery = EventQuery()
        while (!done) {
            val page = eventStore.get(eventQuery)
            for (event in page.data) {
                sendChannel.send(event)
            }

            eventQuery = page.toEventQuery()
            done = page.data.isEmpty()
        }
    }
}

