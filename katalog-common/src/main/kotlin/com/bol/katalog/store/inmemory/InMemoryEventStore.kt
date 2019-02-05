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
        val toIndex = fromIndex + query.pageSize

        val cursor: String? = if (toIndex > events.size) null else toIndex.toString()
        return Page(events.subList(fromIndex, minOf(toIndex, events.size)), cursor)
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