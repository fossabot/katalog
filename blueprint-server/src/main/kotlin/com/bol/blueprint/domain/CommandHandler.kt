package com.bol.blueprint.domain

import com.bol.blueprint.domain.Event.Companion.event
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
        protected val listeners: List<Sink>
) {
    private val channels: ConcurrentMap<Sink, SendChannel<Event<Any>>> = ConcurrentHashMap()

    suspend fun createNamespace(key: NamespaceKey) {
        publish(event { NamespaceCreatedEvent(key) })
    }

    suspend fun deleteNamespace(key: NamespaceKey) {
        publish(event { NamespaceDeletedEvent(key) })
    }

    suspend fun createSchema(key: SchemaKey, schemaType: SchemaType) {
        publish(event { SchemaCreatedEvent(key, schemaType) })
    }

    suspend fun deleteSchema(key: SchemaKey) {
        publish(event { SchemaDeletedEvent(key) })
    }

    suspend fun createVersion(key: VersionKey) {
        publish(event { VersionCreatedEvent(key) })
    }

    suspend fun deleteVersion(key: VersionKey) {
        publish(event { VersionDeletedEvent(key) })
    }

    suspend fun createArtifact(key: ArtifactKey, mediaType: MediaType, data: ByteArray) {
        val path = key.getBlobStorePath()
        blobStore.store(path, data)
        publish(event { ArtifactCreatedEvent(key, mediaType, path, data) })
    }

    suspend fun deleteArtifact(key: ArtifactKey) {
        val path = key.getBlobStorePath()
        blobStore.delete(path)
        publish(event { ArtifactDeletedEvent(key) })
    }

    private suspend fun <T : Any> publish(event: Event<T>) {
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

    protected suspend fun <T> publishToListeners(event: Event<T>) = listeners.forEach {
        val channel = channels.getOrPut(it) {
            actor {
                for (e in channel) {
                    @Suppress("UNCHECKED_CAST")
                    it.getHandler<T>()(e.metadata, e.data as T)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        channel.send(event as Event<Any>)
    }

    fun reset() {
        listeners.forEach {
            if (it is Resettable) {
                it.reset()
            }
        }
    }
}

