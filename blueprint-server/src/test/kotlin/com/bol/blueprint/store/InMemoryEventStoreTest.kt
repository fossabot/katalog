package com.bol.blueprint.store

import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.PersistentEvent
import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.time.Instant

class InMemoryEventStoreTest {
    private val eventStore: EventStore = InMemoryEventStore()

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            eventStore.store(PersistentEvent(at(101, "a"), TestEvent("1")))
            eventStore.store(PersistentEvent(at(102, "b"), TestEvent("2")))
            eventStore.store(PersistentEvent(at(103, "c"), TestEvent("3")))

            val (data, cursor) = eventStore.get(EventQuery(pageSize = 2))
            expectThat(data).containsExactly(
                PersistentEvent(at(101, "a"), TestEvent("1")),
                PersistentEvent(at(102, "b"), TestEvent("2"))
            )

            val (data2, _) = eventStore.get(EventQuery(cursor = cursor))
            expectThat(data2).containsExactly(PersistentEvent(at(103, "c"), TestEvent("3")))
        }
    }

    private fun at(millis: Long, username: String) =
        PersistentEvent.Metadata(timestamp = Instant.ofEpochMilli(millis), username = username)

    data class TestEvent(private val data: String) : Event()
}
