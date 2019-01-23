package com.bol.katalog.cqrs

import com.bol.katalog.TestData
import kotlin.reflect.KType

class TestAggregateContext : AggregateContext {
    private val onCommandHandlers = mutableMapOf<Any, suspend (Command) -> Command.Result>()

    override fun <K, V> getMap(name: String) = mutableMapOf<K, V>()

    override suspend fun <E : Event> persist(event: E, username: String?): PersistentEvent<E> {
        return event.asPersistentEvent(null, TestData.clock)
    }

    override suspend fun <C : Command> send(handlerType: KType, command: C): Command.Result {
        return onCommandHandlers[handlerType]!!.invoke(command)
    }

    override fun onCommand(handlerType: KType, block: suspend (Command) -> Command.Result) {
        onCommandHandlers[handlerType] = block
    }
}