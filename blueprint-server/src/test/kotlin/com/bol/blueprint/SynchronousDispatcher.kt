package com.bol.blueprint

import com.bol.blueprint.domain.Dispatcher
import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Sink
import com.bol.blueprint.store.BlobStore
import com.bol.blueprint.store.EventStore

class SynchronousDispatcher(eventStore: EventStore, blobStore: BlobStore, listeners: List<Sink<Event>>) : Dispatcher(eventStore, blobStore, listeners) {
    override suspend fun publishToListeners(event: Event) = listeners.forEach {
        it.getSyncHandler()(event)
    }
}