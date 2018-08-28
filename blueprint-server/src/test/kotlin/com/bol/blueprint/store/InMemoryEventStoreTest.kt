package com.bol.blueprint.store

import com.bol.blueprint.domain.Event
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.Instant

class InMemoryEventStoreTest {
    private val eventStore: EventStore = InMemoryEventStore()

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            eventStore.store(Event(at(101, "a"), mapOf("foo" to "1")))
            eventStore.store(Event(at(102, "b"), mapOf("foo" to "2")))
            eventStore.store(Event(at(103, "c"), mapOf("foo" to "3")))

            val (data, cursor) = eventStore.get(EventQuery(pageSize = 2))
            Assertions.assertThat(data).containsExactly(Event(at(101, "a"), mapOf("foo" to "1")), Event(at(102, "b"), mapOf("foo" to "2")))

            val (data2, _) = eventStore.get(EventQuery(cursor = cursor))
            Assertions.assertThat(data2).containsExactly(Event(at(103, "c"), mapOf("foo" to "3")))
        }
    }

    private fun at(millis: Long, username: String) = Event.Metadata(timestamp = Instant.ofEpochMilli(millis), username = username)
}
