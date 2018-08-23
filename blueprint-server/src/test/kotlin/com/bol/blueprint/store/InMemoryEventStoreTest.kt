package com.bol.blueprint.store

import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Event.Companion.event
import com.bol.blueprint.domain.UntypedEvent
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant

class InMemoryEventStoreTest {
    private val eventStore: EventStore = InMemoryEventStore()

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            val metadata = Event.Metadata(timestamp = Instant.ofEpochMilli(123))
            eventStore.store(event(metadata) { UntypedEvent(mapOf("foo" to "1")) })
            eventStore.store(event(metadata) { UntypedEvent(mapOf("foo" to "2")) })
            eventStore.store(event(metadata) { UntypedEvent(mapOf("foo" to "3")) })

            val (data, cursor) = eventStore.get(EventQuery(pageSize = 2))
            assertThat(data).containsExactly(event(metadata) { UntypedEvent(mapOf("foo" to "1")) }, event(metadata) { UntypedEvent(mapOf("foo" to "2")) })

            val (data2, _) = eventStore.get(EventQuery(cursor = cursor))
            assertThat(data2).containsExactly(event(metadata) { UntypedEvent(mapOf("foo" to "3")) })
        }
    }
}
