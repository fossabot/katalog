package com.bol.katalog.api.support

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.CqrsAggregate
import com.bol.katalog.cqrs.State
import com.bol.katalog.support.Resettable
import com.bol.katalog.users.UserId

/**
 * A wrapper around an aggregate that allows it to be reset after every test
 */
class ResettableAggregate<S : State>(
    private val aggregateFactory: () -> CqrsAggregate<S>
) : Aggregate<S>, Resettable {
    private var aggregate: CqrsAggregate<S> = aggregateFactory()

    override fun reset() {
        aggregate.unregister()
        aggregate = aggregateFactory()
    }

    override suspend fun <T> readAs(userId: UserId, block: suspend S.() -> T) = aggregate.readAs(userId, block)

    override suspend fun sendAs(userId: UserId, c: Command) = aggregate.sendAs(userId, c)

    override fun directAccess() = aggregate.directAccess()
}