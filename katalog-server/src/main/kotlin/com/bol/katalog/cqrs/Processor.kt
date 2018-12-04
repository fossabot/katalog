package com.bol.katalog.cqrs

import com.bol.katalog.cqrs.commands.CommandHandler
import com.bol.katalog.cqrs.commands.Effect
import com.bol.katalog.cqrs.events.EventPublisher
import com.bol.katalog.domain.Command
import com.bol.katalog.domain.Event
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import org.springframework.stereotype.Component

/**
 * The Processor is the class that is directly used by 'domain' processors.
 * If a command is valid, then some action will be taken that can emit one or more events
 */
@Component
class Processor(
    private val handlers: List<CommandHandler>,
    private val publisher: EventPublisher
) {
    suspend fun <TCommand : Command> apply(command: TCommand) {
        publishToHandlers(command)
    }

    private suspend fun publishToHandlers(command: Command) {
        val completions = handlers
            .map {
                val msg = CommandHandler.UnvalidatedCommand(
                    command = command,
                    valid = CompletableDeferred()
                )

                it.commandHandler.send(msg)

                msg.valid
            }

        val results = completions.awaitAll()

        val events = mutableListOf<Event>()
        val effects = mutableListOf<Effect>()

        results.forEach { result ->
            when (result) {
                is ProcessingResult.Valid -> {
                    result.required.forEach { command -> publishToHandlers(command) }
                    events += result.events
                    effects += result.effects
                }
                is ProcessingResult.Invalid -> throw result.cause
            }
        }

        // Once all results have been evaluated and found to not contain any invalid results,
        // we can trigger all effects and publish all events
        effects.forEach { it.invoke() }
        events.forEach { publisher.publish(it) }
    }
}