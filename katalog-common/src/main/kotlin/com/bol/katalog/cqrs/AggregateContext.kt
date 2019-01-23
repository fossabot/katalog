package com.bol.katalog.cqrs

import kotlin.reflect.KType

interface AggregateContext {
    fun <K, V> getMap(name: String): MutableMap<K, V>
    suspend fun <E : Event> persist(event: E, username: String?): PersistentEvent<E>
    suspend fun <C : Command> send(handlerType: KType, command: C): Command.Result
    fun onCommand(handlerType: KType, block: suspend (Command) -> Command.Result)
    suspend fun onStartup(block: suspend () -> Unit) = block()
}