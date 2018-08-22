package com.bol.blueprint.domain

import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.getBlobStorePath
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class CommandHandler(
        private val eventStore: EventStore,
        private val blobStore: BlobStore,
        protected val listeners: List<Sink<Event>>
) {
    private val channels: ConcurrentMap<Sink<Event>, SendChannel<Event>> = ConcurrentHashMap()

    suspend fun createNamespace(key: NamespaceKey) {
        publish(NamespaceCreatedEvent(Events.metadata(), key))
    }

    suspend fun createSchema(key: SchemaKey, schemaType: SchemaType) {
        publish(SchemaCreatedEvent(Events.metadata(), key, schemaType))
    }

    suspend fun createVersion(key: VersionKey) {
        publish(VersionCreatedEvent(Events.metadata(), key))
    }

    suspend fun createArtifact(key: ArtifactKey, mediaType: MediaType, data: ByteArray) {
        val path = key.getBlobStorePath()
        blobStore.store(path, data)
        publish(ArtifactCreatedEvent(Events.metadata(), key, mediaType, path, data))
    }

    private suspend fun publish(event: Event) {
        eventStore.store(event)
        publishToListeners(event)
    }

    suspend fun replayFromStore() {
        var done = false
        var eventQuery = EventQuery()
        while (!done) {
            val page = eventStore.get(eventQuery)
            for (event in page.data) {
                publishToListeners(event)
            }

            eventQuery = page.toEventQuery()
            done = page.data.isEmpty()
        }
    }

    protected suspend fun publishToListeners(event: Event) = listeners.forEach {
        val channel = channels.getOrPut(it) {
            actor {
                for (e in channel) {
                    it.getHandler()(e)
                }
            }
        }
        channel.send(event)
    }
}

