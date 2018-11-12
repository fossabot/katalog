package com.bol.blueprint.queries

import com.bol.blueprint.domain.Event
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

interface Sink {
    fun getHandler(): SendChannel<HandlerMessage<Any>>
}

data class HandlerMessage<T>(val context: HandlerContext, val event: T, val completed: CompletableDeferred<Boolean>)
data class HandlerContext(val metadata: Event.Metadata)