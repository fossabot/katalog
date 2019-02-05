package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.support.CounterIncreasedEvent
import com.bol.katalog.cqrs.support.TestAggregate
import com.bol.katalog.cqrs.support.TestAggregateContext
import com.bol.katalog.security.SystemUser
import com.bol.katalog.store.inmemory.InMemoryEventStore
import com.bol.katalog.testing.TestData
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EventReplayerTest {
    private val agg1 = TestAggregate(TestAggregateContext())
    private val agg2 = TestAggregate(TestAggregateContext())

    private val eventStore = InMemoryEventStore()
    private val replayer = EventReplayer(eventStore, listOf(agg1, agg2))

    @Test
    fun `Can replay events`() {
        runBlocking {
            // Store two events for agg1, one event for agg2
            eventStore.store(
                CounterIncreasedEvent.asPersistentEvent(
                    SystemUser.get().id,
                    TestData.clock
                )
            )
            eventStore.store(
                CounterIncreasedEvent.asPersistentEvent(
                    SystemUser.get().id,
                    TestData.clock
                )
            )
            eventStore.store(
                CounterIncreasedEvent.asPersistentEvent(
                    SystemUser.get().id,
                    TestData.clock
                )
            )
            replayer.replay()

            expectThat(agg1.readAs(SystemUser.get().id) { counter }).isEqualTo(3)
            expectThat(agg2.readAs(SystemUser.get().id) { counter }).isEqualTo(3)
        }
    }
}