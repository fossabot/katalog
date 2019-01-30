package com.bol.katalog.cqrs

import com.bol.katalog.users.UserId
import java.time.Clock
import java.time.Instant

data class PersistentEvent<out T : Event>(val metadata: Metadata, val data: T) {
    data class Metadata(val timestamp: Instant, val userId: UserId)
}

fun <E : Event> E.asPersistentEvent(userId: UserId, clock: Clock) = PersistentEvent(
    metadata = PersistentEvent.Metadata(
        timestamp = clock.instant(),
        userId = userId
    ), data = this
)
