package com.bol.katalog.cqrs

import com.bol.katalog.users.UserId
import kotlin.reflect.KType

interface AggregateContext {
    fun <K, V> getMap(name: String): MutableMap<K, V>
    suspend fun <E : Event> persist(event: E, userId: UserId): PersistentEvent<E>
    suspend fun <C : Command> send(handlerType: KType, command: C, metadata: Command.Metadata): Command.Result
    fun onCommand(handlerType: KType, block: suspend (Command, Command.Metadata) -> Command.Result)
}