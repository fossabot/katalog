package com.bol.blueprint.domain

import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.getBlobStorePath
import org.springframework.stereotype.Component

@Component
class Dispatcher(
        private val eventStore: EventStore,
        private val blobStore: BlobStore,
        protected val listeners: List<Sink<Event>>
) {
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
        publish(ArtifactCreatedEvent(Events.metadata(), key, mediaType, path))
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
        it.getSink().send(event)
    }
}

