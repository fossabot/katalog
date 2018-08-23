package com.bol.blueprint.store

import com.bol.blueprint.domain.Event

class InMemoryEventStore : EventStore {
    private val events = mutableListOf<Event<Any>>()

    override suspend fun get(query: EventQuery): Page<Event<Any>> {
        val fromIndex = query.cursor?.toInt() ?: 0
        var toIndex = fromIndex + query.pageSize
        if (toIndex > events.size) toIndex = events.size

        return Page(events.subList(fromIndex, toIndex), toIndex.toString())
    }

    override suspend fun <T : Any> store(event: Event<T>) {
        @Suppress("UNCHECKED_CAST")
        events += event as Event<Any>
    }
}