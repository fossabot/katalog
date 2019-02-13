package com.bol.katalog.cqrs

import com.bol.katalog.Resettable
import com.bol.katalog.cqrs.hazelcast.AggregateDsl
import com.bol.katalog.cqrs.hazelcast.CoroutineTransactionContext
import com.bol.katalog.cqrs.hazelcast.Transaction
import com.bol.katalog.security.CoroutineUserIdContext
import mu.KotlinLogging
import kotlin.reflect.KClass

abstract class AbstractAggregate(protected val context: AggregateContext) : Resettable, AutoCloseable {
    private val log = KotlinLogging.logger {}

    private var commandHandlers = mutableMapOf<KClass<*>, suspend (Command) -> Unit>()
    private var eventHandlers = mutableMapOf<KClass<*>, suspend (Event, PersistentEvent.Metadata) -> Unit>()

    init {
        context.register(this)
    }

    override fun close() {
        context.unregister(this)
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

