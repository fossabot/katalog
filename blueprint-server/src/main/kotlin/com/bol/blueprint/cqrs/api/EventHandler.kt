package com.bol.blueprint.cqrs.api

import com.bol.blueprint.domain.Event
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

interface EventHandler {
    val eventHandler: SendChannel<CompletedEvent<Any>>

    data class CompletedEvent<T>(
        val context: CompletedEventContext,
        val event: T,
        val completed: CompletableDeferred<Boolean>
    )

    data class CompletedEventContext(val metadata: Event.Metadata)
}