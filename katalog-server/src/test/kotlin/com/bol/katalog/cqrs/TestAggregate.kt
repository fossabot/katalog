package com.bol.katalog.cqrs

import com.bol.katalog.users.UserId
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo

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

        handle<FailingCommand> {
            fail(command.failure)
        }

        handle<ThrowingCommand> {
            throw command.exception
        }

        handle<CausesThrowingEventCommand> {
            event(ThrowingEvent(command.exception))
        }

        handle<CheckUserCommand> {
            event(CheckUserEvent(metadata.userId))
        }
    }

    override fun getEventHandler() = eventHandler {
        handle<CounterIncreasedEvent> {
            this.state.counter++
        }

        handle<CounterDecreasedEvent> {
            this.state.counter--
        }

        handle<ThrowingEvent> {
            throw this.event.exception
        }

        handle<CheckUserEvent> {
            if (metadata.userId != event.userId) {
                fail("Expected userId: $event.userId, got: ${metadata.userId}")
            }
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
data class FailingCommand(val failure: CommandFailure) : Command
data class CausesThrowingEventCommand(val exception: Exception) : Command
data class ThrowingEvent(val exception: Exception) : Event

object CheckUserCommand : Command
data class CheckUserEvent(val userId: UserId) : Event