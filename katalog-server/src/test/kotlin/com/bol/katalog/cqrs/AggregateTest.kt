package com.bol.katalog.cqrs

import com.bol.katalog.TestData
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.clustering.ClusteringContextFactory.Companion.clusteringContextFactoryOf
import com.bol.katalog.cqrs.clustering.inmemory.InMemoryClusteringContext
import kotlinx.coroutines.*
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.CopyOnWriteArrayList

class AggregateTest {
    @Test
    fun `Can handle many messages concurrently, processing them correctly`() {
        val numCoroutines = 50
        val numActionPerCoroutine = 50

        suspend fun CoroutineScope.massiveRun(action: suspend () -> Unit) {
            val jobs = List(numCoroutines) {
                launch {
                    repeat(numActionPerCoroutine) { action() }
                }
            }
            jobs.forEach { it.join() }
        }

        val aggregate = TestAggregate()
        val manager = AggregateManager(
            listOf(aggregate),
            InMemoryEventStore(),
            TestData.clock,
            clusteringContextFactoryOf { InMemoryClusteringContext(it) }
        )
        manager.start().use {
            val counter = runBlocking {
                GlobalScope.massiveRun {
                    aggregate.send(IncreaseCounterCommand)
                }

                aggregate.read { counter }
            }
            expectThat(counter).isEqualTo(numCoroutines * numActionPerCoroutine)
        }
    }

    class TestAggregate : Aggregate<TestState>({ TestState(0) }) {
        override fun getCommandHandler() = commandHandler {
            handle<IncreaseCounterCommand> {
                event(CounterIncreasedEvent)
            }

            handle<RequireIncreaseIfOdd> {
                if (state.counter % 2 == 1) {
                    val newState = require(IncreaseCounterCommand)
                    expectThat(newState.counter % 2).isEqualTo(0)
                }
            }

            handle<DecreaseCounterCommand> {
                event(CounterDecreasedEvent)
            }

            handle<ThrowingCommand> {
                throw command.exception
            }
        }

        override fun getEventHandler() = eventHandler {
            handle<CounterIncreasedEvent> {
                this.state.counter++
            }

            handle<CounterDecreasedEvent> {
                this.state.counter--
            }
        }
    }

    data class TestState(var counter: Int) : State

    object IncreaseCounterCommand : Command
    object RequireIncreaseIfOdd : Command
    object DecreaseCounterCommand : Command

    object CounterIncreasedEvent : Event
    object CounterDecreasedEvent : Event

    data class ThrowingCommand(val exception: Exception) : Command
}
