package com.bol.blueprint

class InMemoryEventStore : EventStore {
    override fun get(query: EventQuery): Page<EventBase> {
        throw NotImplementedError()
    }

    override fun store(event: EventBase) {
    }
}