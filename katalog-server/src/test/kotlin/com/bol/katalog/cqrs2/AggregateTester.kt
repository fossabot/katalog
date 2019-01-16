package com.bol.katalog.cqrs2

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.State
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
            aggregate.send(command).await()
            events.forEach { expectedEvents.add(it) }
        }
    }

    inline fun <reified E : Throwable> sendCatching(command: Command) {
        runBlocking {
            expectThat(catching {
                aggregate.send(command).await()
            }).throws<E>()
        }
    }

    companion object {
        fun <T : Aggregate<S>, S : State> test(aggregate: T, block: AggregateTester<T, S>.() -> Unit): S {
            val tester = AggregateTester(aggregate)

            // Run test code
            aggregate.start()
            block.invoke(tester)
            runBlocking { aggregate.stop() }

            // Grab the current state
            val state = runBlocking { aggregate.read { this } }

            // Check if all events that are expected are indeed there
            val actualEvents =
                runBlocking { aggregate.eventStore.get(EventQuery(pageSize = tester.expectedEvents.size + 1)) }
            expectThat(tester.expectedEvents).containsExactly(actualEvents.data.map { it.data })

            // Check if we can replay all the events
            aggregate.reset()
            val replayedState = runBlocking {
                aggregate.replayFromStore()
                aggregate.read { this }
            }
            expectThat(replayedState).isEqualTo(state)

            return state
        }
    }
}