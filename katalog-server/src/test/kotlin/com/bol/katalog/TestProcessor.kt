package com.bol.katalog

import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.CommandProcessor

class TestProcessor(private val delegate: CommandProcessor) : CommandProcessor {
    val received: MutableList<Command> = mutableListOf()

    override suspend fun <TCommand : Command> apply(command: TCommand) {
        received += command
        delegate.apply(command)
    }

    fun clearReceivedEvents() {
        received.clear()
    }
}
