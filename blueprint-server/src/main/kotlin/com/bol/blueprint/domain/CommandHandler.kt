package com.bol.blueprint.domain

import com.bol.blueprint.queries.Resettable
import com.bol.blueprint.queries.Sink
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.getBlobStorePath
import kotlinx.coroutines.experimental.reactive.awaitFirstOrNull
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class CommandHandler(
    private val eventStore: EventStore,
    private val blobStore: BlobStore,
    protected val listeners: List<Sink>
) {
    suspend fun createNamespace(key: NamespaceKey) {
        publish(NamespaceCreatedEvent(key))
    }

    suspend fun deleteNamespace(key: NamespaceKey) {
        publish(NamespaceDeletedEvent(key))
    }

    suspend fun createSchema(key: SchemaKey, schemaType: SchemaType) {
        publish(SchemaCreatedEvent(key, schemaType))
    }

    suspend fun deleteSchema(key: SchemaKey) {
        publish(SchemaDeletedEvent(key))
    }

    suspend fun createVersion(key: VersionKey) {
        publish(VersionCreatedEvent(key))
    }

    suspend fun deleteVersion(key: VersionKey) {
        publish(VersionDeletedEvent(key))
    }

    suspend fun createArtifact(key: ArtifactKey, mediaType: MediaType, data: ByteArray) {
        val path = key.getBlobStorePath()
        blobStore.store(path, data)
        publish(ArtifactCreatedEvent(key, mediaType, path, data))
    }

    suspend fun deleteArtifact(key: ArtifactKey) {
        val path = key.getBlobStorePath()
        blobStore.delete(path)
        publish(ArtifactDeletedEvent(key))
    }

    private suspend fun <T : Any> publish(eventData: T) {
        val userDetails = ReactiveSecurityContextHolder.getContext().awaitFirstOrNull()?.authentication?.principal as UserDetails?
        val event = Event(metadata = Event.Metadata(username = userDetails?.username ?: "Unknown"), data = eventData)
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
        it.getHandler<T>()(event.metadata, event.data)
    }

    fun reset() {
        listeners.forEach {
            if (it is Resettable) {
                it.reset()
            }
        }
    }
}

