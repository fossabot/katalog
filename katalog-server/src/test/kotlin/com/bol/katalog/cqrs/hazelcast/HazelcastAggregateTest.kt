package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.AbstractAggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.ForbiddenException
import com.bol.katalog.cqrs.support.*
import com.bol.katalog.security.support.user1
import com.bol.katalog.support.AggregateTester
import com.bol.katalog.support.TestHazelcastAggregateContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class HazelcastAggregateTest {
    private val tester = AggregateTester.of { ctx, _ ->
        listOf(TestAggregate(ctx))
    }

    @Test
    fun `Commands trigger events`() {
        tester.run {
            send(IncreaseCounterCommand)
            expect {
                event(CounterIncreasedEvent)
                state<TestAggregate> {
                    expectThat(it.getCounter()).isEqualTo(1)
                }
            }
        }
    }

    @Test
    fun `Exceptions in commands bubble up`() {
        tester.run {
            send(ThrowingCommand(IllegalStateException("error!")))
            expect {
                throws<IllegalStateException>("error!")
            }
        }
    }

    @Test
    fun `Exceptions in events bubble up`() {
        tester.run {
            send(CausesThrowingEventCommand(ForbiddenException("error!")))
            expect {
                throws<ForbiddenException>("error!")
            }
        }
    }

    @Test
    fun `Events receive the correct userId`() {
        tester.run {
            sendAs(user1, CheckUserCommand)
            expect {
                event(CheckUserEvent(user1.id))
            }
        }
    }

    @Test
    fun `Published events are received by all aggregates`() {
        TestHazelcastAggregateContext.get().use { context ->
            val agg1 = TestAggregate(context, "agg1")

            var received = false

            class OtherAggregate(context: AggregateContext) : AbstractAggregate(context) {
                init {
                    setup {
                        event<CounterIncreasedEvent> {
                            received = true
                        }
                    }
                }

                override suspend fun reset() {
                }
            }

            val agg2 = OtherAggregate(context)

            runBlocking {
                context.sendAs(user1.id, IncreaseCounterCommand)
                expectThat(agg1.getCounter()).isEqualTo(1)
                expectThat(received).isTrue()
                expectThat(context.getRegisteredAggregates()).containsExactlyInAnyOrder(agg1, agg2)
            }
        }
    }

    @Test
    fun `Required commands are sent to all aggregates`() {
        TestHazelcastAggregateContext.get().use { context ->
            val test = TestAggregate(context)

            // Define an aggregate that will require() a command from TestAggregate
            class RequireCommand : Command

            class OtherAggregate(context: AggregateContext) : AbstractAggregate(context) {
                init {
                    setup {
                        command<RequireCommand> {
                            require(IncreaseCounterCommand)
                        }
                    }
                }

                override suspend fun reset() {
                }
            }

            OtherAggregate(context).use { _ ->
                runBlocking {
                    // When sending this command, 'OtherAggregate' will require() IncreaseCounterCommand
                    // Which should be handled by 'test'
                    context.sendAs(user1.id, RequireCommand())
                    expectThat(test.getCounter()).isEqualTo(1)
                }
            }
        }
    }
}