package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserIdContext

suspend fun <S : State, T> Aggregate<S>.read(block: suspend S.() -> T): T {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to read state without a user!")
    return readAs(user, block)
}

suspend fun <S : State> Aggregate<S>.send(c: Command) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to send a command without a user!")
    sendAs(user, c)
}
