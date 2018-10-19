package com.bol.blueprint.domain

import com.bol.blueprint.queries.HandlerContext
import com.bol.blueprint.queries.Resettable
import com.bol.blueprint.queries.Sink
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.getBlobStorePath
import kotlinx.coroutines.experimental.runBlocking
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CommandHandler(
        private val eventStore: EventStore,
        private val blobStore: BlobStore,
        protected val listeners: List<Sink>,
        private val userDetailsSupplier: CurrentUserSupplier
) {
    init {
        runBlocking {
            replayFromStore()
        }
    }

    suspend fun createNamespace(key: NamespaceKey, owner: GroupKey, name: String) {
        publish(NamespaceCreatedEvent(key, owner, name))
    }

    suspend fun deleteNamespace(key: NamespaceKey) {
        publish(NamespaceDeletedEvent(key))
    }

    suspend fun createSchema(namespace: NamespaceKey, key: SchemaKey, name: String, schemaType: SchemaType) {
        publish(SchemaCreatedEvent(namespace, key, name, schemaType))
    }

    suspend fun deleteSchema(key: SchemaKey) {
        publish(SchemaDeletedEvent(key))
    }

    suspend fun createVersion(schema: SchemaKey, key: VersionKey, version: String) {
        publish(VersionCreatedEvent(schema, key, version))
    }

    suspend fun deleteVersion(key: VersionKey) {
        publish(VersionDeletedEvent(key))
    }

    suspend fun createArtifact(version: VersionKey, key: ArtifactKey, filename: String, mediaType: MediaType, data: ByteArray) {
        val path = key.getBlobStorePath()
        blobStore.store(path, data)
        publish(ArtifactCreatedEvent(version, key, filename, mediaType, path, data))
    }

    suspend fun deleteArtifact(key: ArtifactKey) {
        val path = key.getBlobStorePath()
        blobStore.delete(path)
        publish(ArtifactDeletedEvent(key))
    }

    private suspend fun <T : Any> publish(eventData: T) {
        val userDetails = userDetailsSupplier.getCurrentUser()
        val event = Event(metadata = Event.Metadata(
                timestamp = Instant.now(),
                username = userDetails?.username ?: "unknown"
        ), data = eventData)
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

    protected suspend fun <T : Any> publishToListeners(event: Event<T>) = listeners.forEach {
        it.getHandler<T>()(HandlerContext(event.metadata), event.data)
    }

    fun reset() {
        listeners.forEach {
            if (it is Resettable) {
                it.reset()
            }
        }
    }
}

