package com.bol.katalog.cqrs

import com.bol.katalog.TestData
import com.bol.katalog.users.UserId
import kotlin.reflect.KType

class TestAggregateContext : AggregateContext {
    private val onCommandHandlers = mutableMapOf<Any, suspend (Command, Command.Metadata) -> Command.Result>()

    var onEvent: ((Event) -> Unit)? = null

    override fun <K, V> getMap(name: String) = mutableMapOf<K, V>()

    override suspend fun <E : Event> persist(event: E, userId: UserId): PersistentEvent<E> {
        onEvent?.invoke(event)
        return event.asPersistentEvent(userId, TestData.clock)
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
}