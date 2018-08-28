package com.bol.blueprint.domain

import java.time.Instant

data class Event<T>(val metadata: Metadata = Event.Metadata(), val data: T) {
    data class Metadata(val timestamp: Instant = Instant.now(), val username: String = "Unknown")
}
