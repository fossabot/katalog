package com.bol.katalog.cqrs2

import com.bol.katalog.TestData
import com.bol.katalog.config.inmemory.InMemoryEventStore
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.cqrs.State
import com.bol.katalog.cqrs2.AggregateTester.Companion.test
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
                deferreds += aggregate.send(IncreaseCounterCommand)
                deferreds += aggregate.send(DecreaseCounterCommand)
                deferreds += aggregate.send(IncreaseCounterCommand)
            }
            expectThat(deferreds.size).isEqualTo(numCoroutines * numActionPerCoroutine * 3)
            deferreds.awaitAll()

            aggregate.read { counter }
        }

        expectThat(counter).isEqualTo(numCoroutines * numActionPerCoroutine)
        runBlocking { aggregate.stop() }
    }

    @Test
    fun `Can use AggregateStarter to start and stop`() {
        // Create an empty aggregate and pretend an event was already stored in a previous run
        val agg = TestAggregate()
        val metadata = PersistentEvent.Metadata(TestData.clock.instant(), "unknown")
        runBlocking { agg.eventStore.store(PersistentEvent(metadata, CounterIncreasedEvent)) }

        // Aggregate starter should start and stop the aggregate and have it replay the previous events
        // Also, send a command to check it's handled properly and the aggregate is really started
        val starter = AggregateStarter(listOf(agg))
        starter.start()
        runBlocking { agg.send(IncreaseCounterCommand).await() }
        starter.stop()

        val counter = runBlocking { agg.read { counter } }
        expectThat(counter).isEqualTo(2)
    }

    class TestAggregate : Aggregate<TestState>(InMemoryEventStore(), TestData.clock, TestState(0)) {
        override fun getCommandHandler() = commandHandler {
            handle<IncreaseCounterCommand> {
                event(CounterIncreasedEvent)
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
                this.state.copy(counter = this.state.counter + 1)
            }

            handle<CounterDecreasedEvent> {
                this.state.copy(counter = this.state.counter - 1)
            }
        }
    }

    data class TestState(val counter: Int) : State()

    object IncreaseCounterCommand : Command()
    object DecreaseCounterCommand : Command()

    object CounterIncreasedEvent : Event()
    object CounterDecreasedEvent : Event()

    data class ThrowingCommand(val exception: Exception) : Command()
}
