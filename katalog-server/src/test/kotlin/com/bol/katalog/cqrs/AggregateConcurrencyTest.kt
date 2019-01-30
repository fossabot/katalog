package com.bol.katalog.cqrs

import com.bol.katalog.TestData
import com.bol.katalog.security.SystemUser
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
        val numCoroutines = 150
        val numActionPerCoroutine = 150

        suspend fun GlobalScope.massiveRun(action: suspend CoroutineScope.() -> Unit) {
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
                    aggregate.sendAs(SystemUser.get().id, IncreaseCounterCommand)
                }

                aggregate.read { counter }
            }
            expectThat(counter).isEqualTo(numCoroutines * numActionPerCoroutine)
        }
    }
}