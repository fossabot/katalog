package com.bol.katalog.cqrs

import com.bol.katalog.coroutines.Lockable
import com.bol.katalog.users.UserId
import kotlin.reflect.KType

interface AggregateContext : Lockable {
    fun <S : State> register(aggregate: Aggregate<S>)
    fun <S : State> unregister(aggregate: Aggregate<S>)
    fun <K, V> getMap(name: String): MutableMap<K, V>
    suspend fun <E : Event> publish(event: E, userId: UserId)
    suspend fun <C : Command> require(command: C, metadata: Command.Metadata): Command.Result
    suspend fun <C : Command> send(handlerType: KType, command: C, metadata: Command.Metadata): Command.Result
    fun onCommand(handlerType: KType, block: suspend (Command, Command.Metadata) -> Command.Result)
}