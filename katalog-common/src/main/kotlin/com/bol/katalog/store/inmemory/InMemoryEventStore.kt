package com.bol.katalog.store.inmemory

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.Page

class InMemoryEventStore : EventStore {
    private val events = mutableListOf<PersistentEvent<Event>>()

    override suspend fun get(query: EventQuery): Page<PersistentEvent<Event>> {
        val fromIndex = query.cursor?.toInt() ?: 0
        var toIndex = fromIndex + query.pageSize
        if (toIndex > events.size) toIndex = events.size

        return Page(events.subList(fromIndex, toIndex), toIndex.toString())
    }

    override suspend fun <T : Event> store(event: PersistentEvent<T>): PersistentEvent<T> {
        events += event
        return event
    }

    fun getAll() = events

    fun reset() {
        events.clear()
    }
}