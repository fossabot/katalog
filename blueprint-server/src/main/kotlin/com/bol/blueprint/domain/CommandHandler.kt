package com.bol.blueprint.domain

import com.bol.blueprint.queries.Resettable
import com.bol.blueprint.queries.Sink
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.getBlobStorePath
import org.springframework.stereotype.Component
import java.security.Principal

@Component
class CommandHandler(
    private val eventStore: EventStore,
    private val blobStore: BlobStore,
    protected val listeners: List<Sink>
) : PrincipalEnforcingCommandHandler {
    private var principal: String? = null

    override suspend fun withPrincipal(principal: Principal, block: suspend CommandHandler.() -> Unit) {
        val handler = CommandHandler(eventStore, blobStore, listeners)
        handler.principal = principal.name
        block.invoke(handler)
    }

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
        val event = Event(metadata = Event.Metadata(username = principal ?: "Unknown"), data = eventData)
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

