package com.bol.blueprint.domain

import kotlinx.coroutines.experimental.channels.SendChannel
import java.time.Instant

interface Sink<T> {
    fun getSink(): SendChannel<T>
}

interface Event {
    val metadata: Metadata

    data class Metadata(val timestamp: Instant)
}

object Events {
    fun metadata() = Event.Metadata(timestamp = Instant.now())
}

data class UntypedEvent(override val metadata: Event.Metadata, val data: Map<String, Any>) : Event
