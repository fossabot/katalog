package com.bol.blueprint.store

import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.PersistentEvent

interface EventStore {
    suspend fun get(query: EventQuery): Page<PersistentEvent<Event>>
    suspend fun <T : Event> store(event: PersistentEvent<T>)
}

data class EventQuery(val pageSize: Int = 50, val cursor: String? = null)

data class Page<T>(val data: Collection<T>, val nextPageCursor: String?) {
    fun toEventQuery(pageSize: Int = 50) = EventQuery(pageSize, nextPageCursor)
}