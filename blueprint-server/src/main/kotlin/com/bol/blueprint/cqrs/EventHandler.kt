package com.bol.blueprint.cqrs

import com.bol.blueprint.domain.Event
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

interface EventHandler {
    fun getEventHandlerChannel(): SendChannel<CompletedEvent<Any>>
}

data class CompletedEvent<T>(
    val context: CompletedEventContext,
    val event: T,
    val completed: CompletableDeferred<Boolean>
)

data class CompletedEventContext(val metadata: Event.Metadata)