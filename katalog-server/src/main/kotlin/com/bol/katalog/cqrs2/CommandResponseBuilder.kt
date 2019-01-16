package com.bol.katalog.cqrs2

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.Event

data class CommandResponse(val events: List<Event>)

class CommandResponseBuilder<T : Command>(val command: T) {
    private val events = mutableListOf<Event>()

    fun getResponse() = CommandResponse(events)

    fun event(event: Event) {
        events += event
    }
}
