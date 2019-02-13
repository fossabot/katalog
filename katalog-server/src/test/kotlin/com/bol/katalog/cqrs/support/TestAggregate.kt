package com.bol.katalog.cqrs.support

import com.bol.katalog.cqrs.AbstractAggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.users.UserId
import kotlin.test.fail

class TestAggregate(context: AggregateContext, private val stateId: String = "state") :
    AbstractAggregate(context) {
    init {
        setup {
            command<IncreaseCounterCommand> {
                event(CounterIncreasedEvent)
            }

            command<ThrowingCommand> {
                throw command.exception
            }

            command<CausesThrowingEventCommand> {
                event(ThrowingEvent(command.exception))
            }

            command<CheckUserCommand> {
                event(CheckUserEvent(userId))
            }

            event<CounterIncreasedEvent> {
                context.map<String, Int>(stateId).write {
                    val value = getForUpdate("counter")
                    this["counter"] = (value ?: 0) + 1
                }
            }

            event<ThrowingEvent> {
                throw event.exception
            }

            event<CheckUserEvent> {
                if (userId != event.userId) {
                    fail("Expected userId: $event.userId, got: $userId")
                }
            }
        }
    }

    suspend fun getCounter() = context.map<String, Int>(stateId).read { this["counter"] ?: 0 }

    override suspend fun reset() {
    }
}

object IncreaseCounterCommand : Command

object CounterIncreasedEvent : Event

data class ThrowingCommand(val exception: Exception) : Command
data class CausesThrowingEventCommand(val exception: Exception) : Command
data class ThrowingEvent(val exception: Exception) : Event

object CheckUserCommand : Command
data class CheckUserEvent(val userId: UserId) : Event