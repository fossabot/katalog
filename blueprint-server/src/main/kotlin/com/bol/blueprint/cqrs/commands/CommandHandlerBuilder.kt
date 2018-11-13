package com.bol.blueprint.cqrs.commands

import com.bol.blueprint.domain.Command
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class CommandHandlerBuilder {
    val validators: MutableMap<String, (Command) -> CommandValidationFailure?> = mutableMapOf()

    // Simple DSL to be able to easily return different CommandValidationFailures
    class CommandHandlerBuilderValidationStep {
        fun valid(): CommandValidationFailure? = null
        fun conflict() = CommandValidationFailure.Conflict("Conflict")
        fun unknown() = CommandValidationFailure.UnknownProblem("Conflict")
    }

    inline fun <reified T : Command> validate(crossinline block: CommandHandlerBuilderValidationStep.(T) -> CommandValidationFailure?) {
        validators[T::class.java.name] = { command ->
            block.invoke(CommandHandlerBuilderValidationStep(), command as T)
        }
    }

    companion object {
        fun commandHandler(block: CommandHandlerBuilder.() -> Unit): SendChannel<CommandHandler.UnvalidatedCommand<Command>> {
            val builder = CommandHandlerBuilder()
            block.invoke(builder)

            val channel = Channel<CommandHandler.UnvalidatedCommand<Command>>()

            // Coroutine will exit when the channel closes
            GlobalScope.launch {
                for (message in channel) {
                    try {
                        val validationResult =
                            builder.validators[message.command::class.java.name]?.invoke(message.command)
                        message.valid.complete(validationResult)
                    } catch (e: Exception) {
                        message.valid.completeExceptionally(e)
                    }
                }
            }

            return channel
        }
    }
}