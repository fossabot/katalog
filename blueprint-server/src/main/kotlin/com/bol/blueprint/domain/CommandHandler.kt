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

    suspend fun createNamespace(id: NamespaceId, owner: GroupId, name: String) {
        publish(NamespaceCreatedEvent(id, owner, name))
    }

    suspend fun deleteNamespace(id: NamespaceId) {
        publish(NamespaceDeletedEvent(id))
    }

    suspend fun createSchema(namespaceId: NamespaceId, id: SchemaId, name: String, schemaType: SchemaType) {
        publish(SchemaCreatedEvent(namespaceId, id, name, schemaType))
    }

    suspend fun deleteSchema(id: SchemaId) {
        publish(SchemaDeletedEvent(id))
    }

    suspend fun createVersion(schemaId: SchemaId, id: VersionId, version: String) {
        publish(VersionCreatedEvent(schemaId, id, version))
    }

    suspend fun deleteVersion(id: VersionId) {
        publish(VersionDeletedEvent(id))
    }

    suspend fun createArtifact(versionId: VersionId, id: ArtifactId, filename: String, mediaType: MediaType, data: ByteArray) {
        val path = id.getBlobStorePath()
        blobStore.store(path, data)
        publish(ArtifactCreatedEvent(versionId, id, filename, mediaType, data))
    }

    suspend fun deleteArtifact(id: ArtifactId) {
        val path = id.getBlobStorePath()
        blobStore.delete(path)
        publish(ArtifactDeletedEvent(id))
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

