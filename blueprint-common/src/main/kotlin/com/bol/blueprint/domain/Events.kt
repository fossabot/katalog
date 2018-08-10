package com.bol.blueprint.domain

import kotlinx.coroutines.experimental.channels.SendChannel

interface Sink<T> {
    fun getSink(): SendChannel<T>
}

abstract class Event
data class UntypedEvent(val data: Map<String, Any>) : Event()
