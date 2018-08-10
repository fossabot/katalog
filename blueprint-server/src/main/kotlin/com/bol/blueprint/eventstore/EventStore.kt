package com.bol.blueprint.eventstore

import com.bol.blueprint.Event

interface EventStore {
    fun get(query: EventQuery): Page<Event>
    fun store(event: Event)
}

data class EventQuery(val pageSize: Int = 50, val afterId: Long? = null)

data class Page<T>(val data: Collection<T>, val nextPageAfterId: Long)