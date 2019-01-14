package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.commands.Effect

class ProcessingResultBuilder<T : Command>(val command: T) {
    private val requires = mutableListOf<Command>()
    private val events = mutableListOf<Event>()
    private val effects = mutableListOf<Effect>()

    fun require(command: Command) {
        requires += command
    }

    fun event(event: Event) {
        events += event
    }

    fun effect(effect: Effect) {
        effects += effect
    }

    fun complete() = ProcessingResult.Valid(requires, events, effects)
    fun invalid(cause: Throwable) = ProcessingResult.Invalid(cause)
}