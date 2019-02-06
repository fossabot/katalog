package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.support.*
import com.bol.katalog.security.support.user1
import com.bol.katalog.support.AggregateTester
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo

class CqrsAggregateTest {
    private val tester = AggregateTester.of { ctx, _ ->
        listOf(TestAggregate(ctx))
    }

    @Test
    fun `Commands trigger events`() {
        tester.run {
            send(IncreaseCounterCommand)
            expect {
                event(CounterIncreasedEvent)
                state<TestState> {
                    expectThat(it.counter).isEqualTo(1)
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
    fun `Failures in commands bubble up`() {
        tester.run {
            send(FailingCommand(CommandFailure.Forbidden("oh no")))
            expect {
                throws<ForbiddenException>("oh no")
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
        val context = TestAggregateContext()

        // Create two identical aggregates.
        // They should both handle any CounterIncreasedEvents that are being published from 'IncreaseCounterCommand'
        val agg1 = TestAggregate(context)
        val agg2 = TestAggregate(context)

        // This aggregate doesn't know how to handle the event but that should be fine, too
        class EmptyState : State

        class EmptyAggregate(context: AggregateContext) : CqrsAggregate<EmptyState>(
            context,
            EmptyState()
        ) {
            override fun getCommandHandler() = commandHandler {}
            override fun getEventHandler() = eventHandler {}
        }

        val agg3 = EmptyAggregate(context)

        runBlocking {
            agg1.sendAs(user1.id, IncreaseCounterCommand)
            expectThat(agg1.readAs(user1.id) { counter }).isEqualTo(1)
            expectThat(agg2.readAs(user1.id) { counter }).isEqualTo(1)
            expectThat(context.getRegisteredAggregates()).containsExactlyInAnyOrder(agg1, agg2, agg3)
        }
    }

    @Test
    fun `Required commands are sent to all aggregates`() {
        val context = TestAggregateContext()

        val test = TestAggregate(context)

        // Define an aggregate that will require() a command from TestAggregate
        class EmptyState : State

        class RequireCommand : Command

        class OtherAggregate(context: AggregateContext) : CqrsAggregate<EmptyState>(
            context,
            EmptyState()
        ) {
            override fun getCommandHandler() = commandHandler {
                handle<RequireCommand> {
                    require(IncreaseCounterCommand)
                }
            }

            override fun getEventHandler() = eventHandler {}
        }

        val other = OtherAggregate(context)

        runBlocking {
            // When sending this command, 'other' will require() IncreaseCounterCommand
            // Which should be handled by 'test'
            other.sendAs(user1.id, RequireCommand())
            expectThat(test.readAs(user1.id) { counter }).isEqualTo(1)
        }
    }
}