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
        val context = ProcessingContext(handlers)
        context.publishToHandlers(command)

        // Once all (nested) results have been evaluated and found to not contain any invalid results,
        // we can trigger all effects and publish all events
        context.effects.forEach { it.invoke() }
        context.events.forEach { publisher.publish(it) }
    }

    class ProcessingContext(private val handlers: List<CommandHandler>) {
        internal val events = mutableListOf<Event>()
        internal val effects = mutableListOf<Effect>()

        suspend fun publishToHandlers(command: Command) {
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
        }
    }
}