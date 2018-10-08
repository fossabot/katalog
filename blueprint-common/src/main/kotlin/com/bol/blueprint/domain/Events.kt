package com.bol.blueprint.domain

import java.time.Instant

data class Event<T>(val metadata: Metadata, val data: T) {
    data class Metadata(val timestamp: Instant, val username: String)
}
