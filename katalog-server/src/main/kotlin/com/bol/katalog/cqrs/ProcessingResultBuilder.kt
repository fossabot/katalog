package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.commands.Effect
import com.bol.katalog.domain.Event

class ProcessingResultBuilder<T>(val command: T) {
    private val events = mutableListOf<Event>()
    private val effects = mutableListOf<Effect>()

    fun event(event: Event) {
        events += event
    }

    fun effect(effect: Effect) {
        effects += effect
    }

    fun complete() = ProcessingResult.Valid(listOf(), events, effects)
    fun invalid(cause: Throwable) = ProcessingResult.Invalid(cause)
}