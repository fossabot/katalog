package com.bol.blueprint.store

import com.bol.blueprint.domain.Event

class InMemoryEventStore : EventStore {
    private val events = mutableListOf<Event>()

    override suspend fun get(query: EventQuery): Page<Event> {
        val fromIndex = query.cursor?.toInt() ?: 0
        var toIndex = fromIndex + query.pageSize
        if (toIndex > events.size) toIndex = events.size

        return Page(events.subList(fromIndex, toIndex), toIndex.toString())
    }

    override suspend fun store(event: Event) {
        events += event
    }
}