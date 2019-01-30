package com.bol.katalog.cqrs

data class CommandResponse(val events: List<Event>, val failure: Command.Result.Failure?)

class CommandHandlerContext<S : State, out T : Command>(
    val state: S,
    val command: T,
    val metadata: Command.Metadata,
    val requireCallback: suspend (Command) -> S
) {
    private val events = mutableListOf<Event>()
    private var failure: Command.Result.Failure? = null

    fun getResponse() = CommandResponse(events, failure)

    suspend fun require(command: Command): S {
        return requireCallback(command)
    }

    fun fail(failure: Command.Result.Failure) {
        this.failure = failure
    }

    fun event(event: Event) {
        events += event
    }
}
