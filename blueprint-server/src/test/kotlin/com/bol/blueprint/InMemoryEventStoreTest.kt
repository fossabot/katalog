package com.bol.blueprint

import com.bol.blueprint.domain.UntypedEvent
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import com.bol.blueprint.store.InMemoryEventStore
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InMemoryEventStoreTest {
    private val eventStore: EventStore = InMemoryEventStore()

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            eventStore.store(UntypedEvent(mapOf("foo" to "1")))
            eventStore.store(UntypedEvent(mapOf("foo" to "2")))
            eventStore.store(UntypedEvent(mapOf("foo" to "3")))

            val (data, nextPageAfterId) = eventStore.get(EventQuery(pageSize = 2))
            assertThat(data).containsExactly(UntypedEvent(mapOf("foo" to "1")), UntypedEvent(mapOf("foo" to "2")))

            val (data2, _) = eventStore.get(EventQuery(afterId = nextPageAfterId))
            assertThat(data2).containsExactly(UntypedEvent(mapOf("foo" to "3")))
        }
    }
}
