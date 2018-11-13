package com.bol.blueprint.cqrs.events

import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.PersistentEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

interface EventHandler {
    val eventHandler: EventHandlerChannel

    data class CompletedEvent<T : Event>(
        val context: CompletedEventContext,
        val event: T,
        val completed: CompletableDeferred<Boolean>
    )

    data class CompletedEventContext(val metadata: PersistentEvent.Metadata)
}

typealias EventHandlerChannel = SendChannel<EventHandler.CompletedEvent<Event>>