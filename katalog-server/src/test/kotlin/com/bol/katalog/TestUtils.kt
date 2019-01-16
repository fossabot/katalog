package com.bol.katalog

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.State
import com.bol.katalog.security.CoroutineUserContext
import kotlinx.coroutines.runBlocking

fun <S : State, T> Aggregate<S>.readBlocking(block: suspend S.() -> T) = runBlocking {
    read(block)
}

fun <S : State, T> Aggregate<S>.readBlocking(username: String, block: suspend S.() -> T) = runBlocking {
    CoroutineUserContext.set(TestApplication.security.read { findUserByUsername(username) }!!)
    read(block)
}

fun <S : State> Aggregate<S>.sendBlocking(vararg c: Command) = runBlocking {
    send(*c)
}

fun <S : State> Aggregate<S>.sendBlocking(username: String, vararg c: Command) = runBlocking {
    CoroutineUserContext.set(TestApplication.security.read { findUserByUsername(username) }!!)
    send(*c)
}
