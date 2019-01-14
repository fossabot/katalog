package com.bol.katalog.cqrs.events

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
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