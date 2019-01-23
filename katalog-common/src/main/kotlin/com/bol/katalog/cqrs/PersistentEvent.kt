package com.bol.katalog.cqrs

import java.time.Clock
import java.time.Instant

data class PersistentEvent<out T : Event>(val metadata: Metadata, val data: T) {
    data class Metadata(val timestamp: Instant, val username: String)
}

fun <E : Event> E.asPersistentEvent(username: String?, clock: Clock) = PersistentEvent(
    metadata = PersistentEvent.Metadata(
        timestamp = clock.instant(),
        username = username ?: "unknown"
    ), data = this
)
