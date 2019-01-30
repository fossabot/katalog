package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.support.*
import com.bol.katalog.security.support.user1
import com.bol.katalog.support.AggregateTester
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class CqrsAggregateTest {
    private val tester = AggregateTester.of { ctx, _ ->
        TestAggregate(ctx)
    }

    @Test
    fun `Commands trigger events`() {
        tester.run {
            send(IncreaseCounterCommand)
            expect {
                event(CounterIncreasedEvent)
                state {
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
}