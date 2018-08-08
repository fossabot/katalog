package com.bol.blueprint

interface EventStore {
    fun get(query: EventQuery): Page<EventBase>
    fun store(event: EventBase)
}

data class EventQuery(val pageSize: Int = 50, val afterId: Long? = null)

data class Page<T>(val data: Collection<T>, val nextPageAfterId: Long)