package com.bol.blueprint.eventstore

import com.bol.blueprint.domain.Event

class InMemoryEventStore : EventStore {
    private val events = mutableListOf<Event>()

    override fun get(query: EventQuery): Page<Event> {
        val fromIndex = query.afterId?.toInt() ?: 0
        var toIndex = fromIndex + query.pageSize
        if (toIndex > events.size) toIndex = events.size

        return Page(events.subList(fromIndex, toIndex), toIndex.toLong())
    }

    override fun store(event: Event) {
        events += event
    }
}