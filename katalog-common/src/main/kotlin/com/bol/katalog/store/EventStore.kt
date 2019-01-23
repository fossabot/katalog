package com.bol.katalog.store

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.cqrs.asPersistentEvent
import java.time.Clock

interface EventStore {
    suspend fun get(query: EventQuery): Page<PersistentEvent<Event>>
    suspend fun <T : Event> store(event: PersistentEvent<T>): PersistentEvent<T>

    suspend fun <T : Event> store(event: T, username: String?, clock: Clock): PersistentEvent<T> {
        return this.store(event.asPersistentEvent(username, clock))
    }
}

data class EventQuery(val pageSize: Int = 50, val cursor: String? = null)

data class Page<T>(val data: Collection<T>, val nextPageCursor: String?) {
    fun toEventQuery(pageSize: Int = 50) = EventQuery(pageSize, nextPageCursor)
}