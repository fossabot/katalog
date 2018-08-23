package com.bol.blueprint

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Sink
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore

class SynchronousCommandHandler(eventStore: EventStore, blobStore: BlobStore, listeners: List<Sink>) : CommandHandler(eventStore, blobStore, listeners) {
    override suspend fun <T> publishToListeners(event: Event<T>) = listeners.forEach {
        it.getHandler<T>()(event.metadata, event.data)
    }
}