package com.bol.katalog.api

import com.bol.katalog.Resettable
import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.CqrsAggregate
import com.bol.katalog.cqrs.State

/**
 * A wrapper around an aggregate that allows it to be reset after every test
 */
class ResettableAggregate<S : State>(
    private val aggregateFactory: () -> CqrsAggregate<S>
) : Aggregate<S>, Resettable {
    private lateinit var aggregate: Aggregate<S>

    init {
        reset()
    }

    override fun reset() {
        aggregate = aggregateFactory()
    }

    override suspend fun <T> read(block: suspend S.() -> T) = aggregate.read(block)

    override suspend fun send(c: Command) = aggregate.send(c)
}