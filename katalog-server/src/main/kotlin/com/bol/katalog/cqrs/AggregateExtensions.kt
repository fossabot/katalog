package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserIdContext

const val COMMAND_WITHOUT_USER = "Trying to send a command without a user!"

suspend fun AggregateContext.send(c: Command) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException(COMMAND_WITHOUT_USER)
    sendAs(user, c)
}

suspend fun AggregateContext.sendLocal(commands: List<Command>) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException(COMMAND_WITHOUT_USER)
    sendLocalAs(user, commands)
}

suspend fun AggregateContext.sendLocal(vararg commands: Command) {
    val user =
        CoroutineUserIdContext.get() ?: throw IllegalStateException(COMMAND_WITHOUT_USER)
    sendLocalAs(user, commands.toList())
}
