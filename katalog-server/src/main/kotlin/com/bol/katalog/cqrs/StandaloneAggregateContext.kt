package com.bol.katalog.cqrs

import com.bol.katalog.messaging.inmemory.InMemoryQueue
import com.bol.katalog.store.EventStore
import java.time.Clock
import kotlin.reflect.KType

class StandaloneAggregateContext(
    private val eventStore: EventStore,
    private val clock: Clock
) : AggregateContext, AutoCloseable {
    private val queues = mutableMapOf<KType, InMemoryQueue<Command>>()

    override fun <K, V> getMap(name: String) = mutableMapOf<K, V>()

    override suspend fun <E : Event> persist(event: E, username: String?) =
        eventStore.store(event, username, clock)

    override suspend fun <C : Command> send(handlerType: KType, command: C): Command.Result {
        return queues[handlerType]?.send(command)
            ?: throw IllegalStateException("Trying to send to handler type $handlerType that did not register an 'onCommand' handler yet")
    }

    override fun onCommand(handlerType: KType, block: suspend (Command) -> Command.Result) {
        queues[handlerType] = InMemoryQueue(block)
    }

    override fun close() {
        queues.values.forEach { it.close() }
    }
}
