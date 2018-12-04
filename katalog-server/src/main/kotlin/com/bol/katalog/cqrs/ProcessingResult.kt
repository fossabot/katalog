package com.bol.katalog.cqrs

import com.bol.katalog.domain.Command
import com.bol.katalog.domain.Event

sealed class ProcessingResult {
    data class Valid(val required: Collection<Command>, val events: Collection<Event>) : ProcessingResult()
    data class Invalid(val cause: Throwable) : ProcessingResult()
    object NotHandledByProcessor : ProcessingResult()
}