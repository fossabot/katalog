package com.bol.katalog.cqrs

import com.bol.katalog.security.CoroutineUserIdContext

/**
 * DirectAggregate can be used to send commands directly to an aggregate without going through an intermediate
 * queue. This is NOT thread-safe, but it can improve performance.
 *
 * This is used by classes such as EventReplayer at startup.
 */
class DirectAggregate<S : State>(private val aggregate: CqrsAggregate<S>) {
    suspend fun send(command: Command) {
        val userId = CoroutineUserIdContext.get()
            ?: throw IllegalStateException("Trying to send a command without a user!")
        val metadata = Command.Metadata(userId)
        aggregate.invokeCommandHandler(command, metadata)
    }

    suspend fun send(event: Event, metadata: PersistentEvent.Metadata) {
        aggregate.handleEvent(event, metadata)
    }
}