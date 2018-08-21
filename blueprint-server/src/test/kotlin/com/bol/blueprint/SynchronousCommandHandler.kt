package com.bol.blueprint

import com.bol.blueprint.domain.CommandHandler
import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Sink
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore

class SynchronousCommandHandler(eventStore: EventStore, blobStore: BlobStore, listeners: List<Sink<Event>>) : CommandHandler(eventStore, blobStore, listeners) {
    override suspend fun publishToListeners(event: Event) = listeners.forEach {
        it.getHandler()(event)
    }
}