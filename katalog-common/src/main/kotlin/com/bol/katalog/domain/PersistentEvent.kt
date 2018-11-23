package com.bol.katalog.domain

import java.time.Instant

data class PersistentEvent<T : Event>(val metadata: Metadata, val data: T) {
    data class Metadata(val timestamp: Instant, val username: String)
}
