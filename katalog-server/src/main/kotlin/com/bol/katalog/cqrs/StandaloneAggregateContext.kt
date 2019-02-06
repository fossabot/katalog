package com.bol.katalog.cqrs

import com.bol.katalog.coroutines.Lockable
import com.bol.katalog.coroutines.MutexLockable
import com.bol.katalog.store.EventStore
import com.bol.katalog.users.UserId
import java.time.Clock
import kotlin.reflect.KType

class StandaloneAggregateContext(
    private val eventStore: EventStore,
    private val clock: Clock,
    private val lockable: Lockable = MutexLockable()
) : AggregateContext, AutoCloseable, Lockable by lockable {
    private val aggregates = mutableListOf<Aggregate<*>>()
    private val queues = mutableMapOf<KType, InMemoryCommandQueue>()

    override fun <K, V> getMap(name: String) = mutableMapOf<K, V>()

    override fun <S : State> register(aggregate: Aggregate<S>) {
        aggregates += aggregate
    }

    override suspend fun <E : Event> publish(event: E, userId: UserId) {
        val persisted = eventStore.store(event, userId, clock)
        aggregates.forEach { it.directAccess().send(event, persisted.metadata) }
    }

    override suspend fun <C : Command> require(command: C, metadata: Command.Metadata) {
        aggregates.forEach { it.directAccess().require(command, metadata) }
    }

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
