package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserIdContext

suspend fun AggregateContext.send(c: Command) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to send a command without a user!")
    sendAs(user, c)
}

suspend fun AggregateContext.sendLocal(commands: List<Command>) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to send a command without a user!")
    sendLocalAs(user, commands)
}

suspend fun AggregateContext.sendLocal(vararg commands: Command) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException("Trying to send a command without a user!")
    sendLocalAs(user, commands.toList())
}
