package com.bol.katalog.cqrs.hazelcast

import com.bol.katalog.Resettable
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.security.CoroutineUserIdContext
import com.bol.katalog.users.UserId
import mu.KotlinLogging
import kotlin.reflect.KClass

abstract class HazelcastAggregate(protected val context: HazelcastAggregateContext) : Resettable, AutoCloseable {
    private val log = KotlinLogging.logger {}

    private var commandHandlers = mutableMapOf<KClass<*>, suspend (Command) -> Unit>()
    private var eventHandlers = mutableMapOf<KClass<*>, suspend (Event, PersistentEvent.Metadata) -> Unit>()

    init {
        context.register(this)
    }

    override fun close() {
        context.unregister(this)
    }

    suspend fun sendAs(userId: UserId, c: Command) {
        CoroutineUserIdContext.with(userId) {
            context.send(c)
        }
    }

    suspend fun bulkSendLocalAs(userId: UserId, commands: List<Command>) {
        context.bulkSendLocalAs(userId, commands)
    }

    suspend fun handleCommand(command: Command) {
        log.debug("Command received in {}: {}", this, command)

        val commandHandler = commandHandlers[command::class]
        commandHandler?.invoke(command)
    }

    internal suspend fun handleEvent(event: Event, metadata: PersistentEvent.Metadata) {
        log.debug("Event received in {}: {}", this, event)

        CoroutineUserIdContext.with(metadata.userId) {
            val eventHandler = eventHandlers[event::class]
            eventHandler?.invoke(event, metadata)
        }
    }

    protected fun setup(block: AggregateDsl.() -> Unit) {
        val dsl = AggregateDsl(context)
        block(dsl)
        commandHandlers = dsl.commandHandlers
        eventHandlers = dsl.eventHandlers
    }

    suspend inline fun <reified T> tx(block: Transaction.() -> T): T {
        return with(CoroutineTransactionContext.get()!!) {
            block(this)
        }
    }

    override suspend fun reset() {}
}

