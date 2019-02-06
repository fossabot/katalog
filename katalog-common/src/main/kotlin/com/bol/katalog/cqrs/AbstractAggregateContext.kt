package com.bol.katalog.cqrs

import com.bol.katalog.coroutines.Lockable
import com.bol.katalog.coroutines.MutexLockable
import com.bol.katalog.store.EventStore
import com.bol.katalog.users.UserId
import java.time.Clock

abstract class AbstractAggregateContext(
    private val eventStore: EventStore,
    private val clock: Clock,
    private val lockable: Lockable = MutexLockable()
) : AggregateContext, Lockable by lockable {
    private val aggregates = mutableListOf<Aggregate<*>>()

    override fun <S : State> register(aggregate: Aggregate<S>) {
        aggregates += aggregate
    }

    override fun <S : State> unregister(aggregate: Aggregate<S>) {
        aggregates -= aggregate
    }

    override suspend fun <E : Event> publish(event: E, userId: UserId) {
        val persisted = eventStore.store(event, userId, clock)
        aggregates.forEach { it.directAccess().send(event, persisted.metadata) }
    }

    override suspend fun <C : Command> require(command: C, metadata: Command.Metadata): Command.Result {
        val results = aggregates.map { it.directAccess().require(command, metadata) }
        return results.single { it != Command.Result.Unhandled }
    }

}