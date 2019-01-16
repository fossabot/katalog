package com.bol.katalog.cqrs

import com.bol.katalog.TestData
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.AggregateTester.Companion.test
import kotlinx.coroutines.*
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.CopyOnWriteArrayList

class AggregateTest {
    @Test
    fun `Can do basic round-trip`() {
        val state = test(TestAggregate()) {
            send(IncreaseCounterCommand, CounterIncreasedEvent)
        }

        expectThat(state.counter).isEqualTo(1)
    }

    @Test
    fun `Can require other commands`() {
        val state = test(TestAggregate()) {
            send(IncreaseCounterCommand, CounterIncreasedEvent)
            send(RequireIncreaseIfOdd, CounterIncreasedEvent)
            send(RequireIncreaseIfOdd)
        }

        expectThat(state.counter).isEqualTo(2)
    }

    @Test
    fun `Can propagate exceptions and continue correctly`() {
        val state = test(TestAggregate()) {
            sendCatching<UnsupportedOperationException>(ThrowingCommand(UnsupportedOperationException()))
            send(IncreaseCounterCommand, CounterIncreasedEvent)
        }

        expectThat(state.counter).isEqualTo(1)
    }

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
        aggregate.start()
        val counter = runBlocking {
            val deferreds = CopyOnWriteArrayList<Deferred<Unit>>()
            GlobalScope.massiveRun {
                deferreds += aggregate.sendDeferred(
                    IncreaseCounterCommand,
                    DecreaseCounterCommand,
                    IncreaseCounterCommand,
                    RequireIncreaseIfOdd
                )  // after this: counter = 2
            }
            deferreds.awaitAll()

            aggregate.read { counter }
        }

        // we check * 2 because the counter is increased by 2 for every run
        expectThat(counter).isEqualTo(numCoroutines * numActionPerCoroutine * 2)
        runBlocking { aggregate.stop() }
    }

    @Test
    fun `Can use AggregateManager to start and stop`() {
        // Create an empty aggregate
        val agg = TestAggregate()

        // Pretend an event was already stored in a previous run
        val eventStore = InMemoryEventStore()
        val starter = AggregateManager(listOf(agg), eventStore, TestData.clock)
        val metadata = PersistentEvent.Metadata(TestData.clock.instant(), "unknown")
        runBlocking { eventStore.store(PersistentEvent(metadata, CounterIncreasedEvent)) }

        // Aggregate starter should start and stop the aggregate and have it replay the previous events
        // Also, send a command to check it's handled properly and the aggregate is really started
        starter.start()
        runBlocking { agg.send(IncreaseCounterCommand) }
        starter.stop()

        val counter = runBlocking { agg.read { counter } }
        expectThat(counter).isEqualTo(2)
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

    data class TestState(var counter: Int) : State()

    object IncreaseCounterCommand : Command()
    object RequireIncreaseIfOdd : Command()
    object DecreaseCounterCommand : Command()

    object CounterIncreasedEvent : Event()
    object CounterDecreasedEvent : Event()

    data class ThrowingCommand(val exception: Exception) : Command()
}
