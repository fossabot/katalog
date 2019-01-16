package com.bol.katalog.cqrs

import java.time.Instant

data class PersistentEvent<out T : Event>(val metadata: Metadata, val data: T) {
    data class Metadata(val timestamp: Instant, val username: String)
}
