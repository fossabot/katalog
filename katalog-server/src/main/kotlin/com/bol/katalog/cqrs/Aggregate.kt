package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserIdContext
import com.bol.katalog.users.UserId

interface Aggregate<S : State> {
    suspend fun <T> readAs(userId: UserId, block: suspend S.() -> T): T
    suspend fun <T> read(block: suspend S.() -> T): T {
        val user =
            CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to send a command without a user!")
        return readAs(user, block)
    }

    suspend fun sendAs(userId: UserId, c: Command)
    suspend fun send(c: Command) {
        val user =
            CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to send a command without a user!")
        sendAs(user, c)
    }

    fun directAccess(): DirectAggregate<S>
}