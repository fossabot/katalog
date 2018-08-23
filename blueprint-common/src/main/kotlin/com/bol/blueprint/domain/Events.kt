package com.bol.blueprint.domain

import java.time.Instant

data class Event<T>(val metadata: Metadata, val data: T) {
    data class Metadata(val timestamp: Instant)

    companion object {
        fun <T> event(metadata: Metadata, data: () -> T): Event<T> {
            return Event(metadata = metadata, data = data.invoke())
        }

        fun <T> event(data: () -> T): Event<T> {
            return Event(Event.Metadata(timestamp = Instant.now()), data = data.invoke())
        }
    }
}

data class UntypedEvent(val data: Map<String, Any>)
