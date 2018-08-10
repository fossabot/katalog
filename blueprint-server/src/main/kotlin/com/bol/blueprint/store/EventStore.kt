package com.bol.blueprint.store

import com.bol.blueprint.domain.Event

interface EventStore {
    suspend fun get(query: EventQuery): Page<Event>
    suspend fun store(event: Event)
}

data class EventQuery(val pageSize: Int = 50, val afterId: Long? = null)

data class Page<T>(val data: Collection<T>, val nextPageAfterId: Long) {
    fun toEventQuery(pageSize: Int = 50) = EventQuery(pageSize, nextPageAfterId)
}