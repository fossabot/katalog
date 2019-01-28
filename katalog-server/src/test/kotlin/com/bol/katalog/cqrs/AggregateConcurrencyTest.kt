package com.bol.katalog.cqrs

import com.bol.katalog.TestData
import com.bol.katalog.store.inmemory.InMemoryEventStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class AggregateConcurrencyTest {
    @Test
    fun `TestAggregateContext can handle many messages concurrently, processing them correctly`() {
        concurrencyTest(TestAggregateContext())
    }

    @Test
    fun `StandaloneAggregateContext can handle many messages concurrently, processing them correctly`() {
        StandaloneAggregateContext(InMemoryEventStore(), TestData.clock).use { concurrencyTest(it) }
    }

    private fun concurrencyTest(context: AggregateContext) {
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

        val aggregate = TestAggregate(context)
        aggregate.use {
            val counter = runBlocking {
                GlobalScope.massiveRun {
                    aggregate.send(IncreaseCounterCommand)
                }

                aggregate.read { counter }
            }
            expectThat(counter).isEqualTo(numCoroutines * numActionPerCoroutine)
        }
    }

    class TestAggregate(context: AggregateContext) : CqrsAggregate<TestState>(context, TestState(0)) {
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