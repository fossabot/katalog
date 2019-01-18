package com.bol.katalog.cqrs

import com.bol.katalog.TestData
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.clustering.inmemory.InMemoryClusteringContext
import com.bol.katalog.store.EventQuery
import kotlinx.coroutines.runBlocking
import strikt.api.catching
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.throws

class AggregateTester<T : Aggregate<S>, S : State>(val aggregate: T) {
    private val expectedEvents = mutableListOf<Event>()

    fun send(command: Command, vararg events: Event) {
        runBlocking {
            aggregate.send(command)
            events.forEach { expectedEvents.add(it) }
        }
    }

    inline fun <reified E : Throwable> sendCatching(command: Command) {
        runBlocking {
            expectThat(catching {
                aggregate.send(command)
            }).throws<E>()
        }
    }

    companion object {
        fun <T : Aggregate<S>, S : State> test(aggregate: T, block: AggregateTester<T, S>.() -> Unit): S {
            val eventStore = InMemoryEventStore()
            val clustering =
                InMemoryClusteringContext(eventStore, TestData.clock)
            val manager = AggregateManager(listOf(aggregate), eventStore, clustering)
            val tester = AggregateTester(aggregate)

            // Run test code
            manager.start()
            block.invoke(tester)
            manager.stop()

            // Grab the current state
            val state = runBlocking { aggregate.read { this } }

            // Check if all events that are expected are indeed there
            val actualEvents =
                runBlocking { eventStore.get(EventQuery(pageSize = tester.expectedEvents.size + 1)) }
            expectThat(tester.expectedEvents).containsExactly(actualEvents.data.map { it.data })

            // Check if we can replay all the events
            manager.start()
            val replayedState = runBlocking { aggregate.read { this } }
            manager.stop()
            expectThat(replayedState).isEqualTo(state)

            return state
        }
    }
}