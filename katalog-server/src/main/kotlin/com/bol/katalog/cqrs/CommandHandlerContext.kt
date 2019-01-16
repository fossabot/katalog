package com.bol.katalog.cqrs

data class CommandResponse(val events: List<Event>)

class CommandHandlerContext<S : State, out T : Command>(
    val state: S,
    val command: T,
    val requireCallback: suspend (Command) -> S
) {
    private val events = mutableListOf<Event>()

    fun getResponse() = CommandResponse(events)

    suspend fun require(command: Command): S {
        return requireCallback(command)
    }

    fun event(event: Event) {
        events += event
    }
}
