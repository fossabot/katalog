package com.bol.katalog.cqrs

import com.bol.katalog.store.EventStore
import com.bol.katalog.users.UserId
import java.time.Clock
import kotlin.reflect.KType

class StandaloneAggregateContext(
    private val eventStore: EventStore,
    private val clock: Clock
) : AggregateContext, AutoCloseable {
    private val queues = mutableMapOf<KType, InMemoryCommandQueue>()

    override fun <K, V> getMap(name: String) = mutableMapOf<K, V>()

    override suspend fun <E : Event> persist(event: E, userId: UserId) =
        eventStore.store(event, userId, clock)

    override suspend fun <C : Command> send(
        handlerType: KType,
        command: C,
        metadata: Command.Metadata
    ): Command.Result {
        return queues[handlerType]?.send(command, metadata)
            ?: throw IllegalStateException("Trying to send to handler type $handlerType that did not register an 'onCommand' handler yet")
    }

    override fun onCommand(handlerType: KType, block: suspend (Command, Command.Metadata) -> Command.Result) {
        queues[handlerType] = InMemoryCommandQueue(block)
    }

    override fun close() {
        queues.values.forEach { it.close() }
    }
}
