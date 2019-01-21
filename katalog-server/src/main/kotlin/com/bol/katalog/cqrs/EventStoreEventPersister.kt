package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.clustering.asPersistentEvent
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.store.EventStore
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class EventStoreEventPersister(private val eventStore: EventStore, private val clock: Clock) : EventPersister {
    override suspend fun <E : Event> persist(event: E): PersistentEvent<E> {
        val persistentEvent = event.asPersistentEvent(CoroutineUserContext.get(), clock)
        eventStore.store(persistentEvent)
        return persistentEvent
    }
}