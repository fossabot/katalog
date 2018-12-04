package com.bol.katalog.cqrs

import com.bol.katalog.domain.Event

class ProcessingResultBuilder<T>(val command: T) {
    fun event(event: Event) = ProcessingResult.Valid(listOf(), listOf(event))
    fun invalid(cause: Throwable) = ProcessingResult.Invalid(cause)
}