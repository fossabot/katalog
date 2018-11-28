package com.bol.katalog.store

import com.bol.katalog.domain.Event
import com.bol.katalog.domain.PersistentEvent

class InMemoryEventStore : EventStore {
    private val events = mutableListOf<PersistentEvent<Event>>()

    override suspend fun get(query: EventQuery): Page<PersistentEvent<Event>> {
        val fromIndex = query.cursor?.toInt() ?: 0
        var toIndex = fromIndex + query.pageSize
        if (toIndex > events.size) toIndex = events.size

        return Page(events.subList(fromIndex, toIndex), toIndex.toString())
    }

    override suspend fun <T : Event> store(event: PersistentEvent<T>) {
        @Suppress("UNCHECKED_CAST")
        events += event as PersistentEvent<Event>
    }
}