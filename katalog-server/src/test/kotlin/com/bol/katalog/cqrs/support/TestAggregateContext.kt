package com.bol.katalog.cqrs.support

import com.bol.katalog.cqrs.*
import com.bol.katalog.support.TestData
import com.bol.katalog.users.UserId
import kotlin.reflect.KType

class TestAggregateContext : AggregateContext {
    private val aggregates = mutableListOf<Aggregate<*>>()
    private val onCommandHandlers = mutableMapOf<Any, suspend (Command, Command.Metadata) -> Command.Result>()

    var onEvent: ((Event) -> Unit)? = null

    override fun <S : State> register(aggregate: Aggregate<S>) {
        aggregates += aggregate
    }

    override fun <K, V> getMap(name: String) = mutableMapOf<K, V>()

    override suspend fun <E : Event> publish(event: E, userId: UserId) {
        onEvent?.invoke(event)
        aggregates.forEach { it.directAccess().send(event, event.asPersistentEvent(userId, TestData.clock).metadata) }
    }

    override suspend fun <C : Command> send(
        handlerType: KType,
        command: C,
        metadata: Command.Metadata
    ): Command.Result {
        return onCommandHandlers[handlerType]!!.invoke(command, metadata)
    }

    override fun onCommand(handlerType: KType, block: suspend (Command, Command.Metadata) -> Command.Result) {
        onCommandHandlers[handlerType] = block
    }

    fun getRegisteredAggregates() = aggregates.toList()
}