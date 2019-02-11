package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.security.CoroutineUserIdContext
import com.bol.katalog.users.UserId
import java.time.Instant
import kotlin.reflect.KClass

class AggregateDsl(val context: HazelcastAggregateContext) {
    val commandHandlers = mutableMapOf<KClass<*>, suspend (Command) -> Unit>()
    val eventHandlers = mutableMapOf<KClass<*>, suspend (Event, PersistentEvent.Metadata) -> Unit>()

    inline fun <reified C : Command> command(noinline block: suspend CommandHandler<C>.() -> Unit) {
        commandHandlers[C::class] = { command: Command ->
            val userId = CoroutineUserIdContext.get()!!
            val handler = CommandHandler(context, command as C, userId)
            handler(block)
        }
    }

    inline fun <reified E : Event> event(noinline block: suspend EventHandler<E>.() -> Unit) {
        eventHandlers[E::class] = { event, metadata ->
            val userId = CoroutineUserIdContext.get()!!
            val handler = EventHandler(event as E, userId, metadata.timestamp)
            handler(block)
        }
    }

    class CommandHandler<C : Command>(
        private val context: HazelcastAggregateContext,
        val command: C,
        val userId: UserId
    ) {
        suspend operator fun invoke(block: suspend CommandHandler<C>.() -> Unit) {
            block(this)
        }

        suspend fun <E : Event> event(event: E) {
            context.publish(event)
        }

        suspend fun <R : Command> require(command: R) {
            context.handleCommand(command)
        }
    }

    class EventHandler<E : Event>(val event: E, val userId: UserId, val timestamp: Instant) {
        suspend operator fun invoke(block: suspend EventHandler<E>.() -> Unit) {
            block(this)
        }
    }
}