package com.bol.katalog.cqrs

/*
class EventReplayerTest {
    private val agg1: TestAggregate = throw NotImplementedError()//TestAggregate(TestAggregateContext())
    private val agg2: TestAggregate = throw NotImplementedError()//TestAggregate(TestAggregateContext())

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
}*/