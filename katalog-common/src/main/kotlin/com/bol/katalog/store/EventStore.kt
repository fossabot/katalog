package com.bol.katalog.store

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.cqrs.asPersistentEvent
import com.bol.katalog.users.UserId
import java.time.Clock

interface EventStore {
    suspend fun get(query: EventQuery): Page<PersistentEvent<Event>>
    suspend fun <T : Event> store(event: PersistentEvent<T>): PersistentEvent<T>

    suspend fun <T : Event> store(event: T, userId: UserId, clock: Clock): PersistentEvent<T> {
        return this.store(event.asPersistentEvent(userId, clock))
    }
}

data class EventQuery(val pageSize: Int = 50, val cursor: String? = null)

data class Page<T>(val data: Collection<T>, val nextPageCursor: String?) {
    fun toNextPageQuery(pageSize: Int = 50) = EventQuery(pageSize, nextPageCursor)
}