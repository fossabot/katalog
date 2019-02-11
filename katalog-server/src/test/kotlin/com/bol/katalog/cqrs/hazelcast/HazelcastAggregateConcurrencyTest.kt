package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.support.IncreaseCounterCommand
import com.bol.katalog.cqrs.support.TestAggregate
import com.bol.katalog.security.SystemUser
import com.bol.katalog.support.TestHazelcastAggregateContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HazelcastAggregateConcurrencyTest {
    @Test
    fun `HazelcastAggregateContext can handle many messages concurrently, processing them correctly`() {
        TestHazelcastAggregateContext.get().use {
            concurrencyTest(it)
        }
    }

    private fun concurrencyTest(context: HazelcastAggregateContext) {
        val numCoroutines = 150
        val numActionPerCoroutine = 150

        suspend fun massiveRun(action: suspend CoroutineScope.() -> Unit) {
            coroutineScope {
                val jobs = List(numCoroutines) {
                    launch {
                        repeat(numActionPerCoroutine) { action() }
                    }
                }

                jobs.forEach { it.join() }
            }
        }

        val aggregate = TestAggregate(context)
        aggregate.use {
            val counter = runBlocking {
                massiveRun {
                    aggregate.sendAs(SystemUser.get().id, IncreaseCounterCommand)
                }

                aggregate.getCounter()
            }
            expectThat(counter).isEqualTo(numCoroutines * numActionPerCoroutine)
        }
    }
}