package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserIdContext

class DirectCqrsAggregate<S : State>(private val aggregate: CqrsAggregate<S>) : DirectAggregate {
    override suspend fun send(command: Command) {
        val userId = CoroutineUserIdContext.get()
            ?: throw IllegalStateException("Trying to send a command without a user!")
        val metadata = Command.Metadata(userId)
        aggregate.invokeCommandHandler(command, metadata)
    }

    override suspend fun send(event: Event, metadata: PersistentEvent.Metadata) {
        aggregate.handleEvent(event, metadata)
    }
}