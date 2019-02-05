package com.bol.katalog.cqrs

/**
 * DirectAggregate can be used to send commands directly to an aggregate without going through an intermediate
 * queue. This is NOT thread-safe, but it can improve performance.
 *
 * This is used by classes such as EventReplayer at startup.
 */
interface DirectAggregate {
    suspend fun send(command: Command)
    suspend fun send(event: Event, metadata: PersistentEvent.Metadata)
}