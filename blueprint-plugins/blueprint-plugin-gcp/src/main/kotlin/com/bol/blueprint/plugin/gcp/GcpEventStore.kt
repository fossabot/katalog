package com.bol.blueprint.plugin.gcp

import com.bol.blueprint.domain.Event
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.Page

class GcpEventStore : EventStore {
    override suspend fun get(query: EventQuery): Page<Event> {
        throw NotImplementedError()
    }

    override suspend fun store(event: Event) {
    }
}