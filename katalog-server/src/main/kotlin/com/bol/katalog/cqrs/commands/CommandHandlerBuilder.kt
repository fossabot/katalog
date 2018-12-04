package com.bol.katalog.cqrs.commands

import com.bol.katalog.cqrs.ProcessingResult
import com.bol.katalog.cqrs.ProcessingResultBuilder
import com.bol.katalog.domain.Command
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

class CommandHandlerBuilder {
    val validators: MutableMap<String, suspend (ProcessingResultBuilder<Command>) -> ProcessingResult> = mutableMapOf()

    inline fun <reified T : Command> handle(crossinline block: suspend ProcessingResultBuilder<T>.() -> ProcessingResult) {
        validators[T::class.java.name] = { builder ->
            @Suppress("UNCHECKED_CAST")
            block.invoke(builder as ProcessingResultBuilder<T>)
        }
    }

    companion object {
        fun handleCommands(block: CommandHandlerBuilder.() -> Unit): SendChannel<CommandHandler.UnvalidatedCommand<Command>> {
            val builder = CommandHandlerBuilder()
            block.invoke(builder)

            val channel = Channel<CommandHandler.UnvalidatedCommand<Command>>()

            // Coroutine will exit when the channel closes
            GlobalScope.launch {
                for (message in channel) {
                    try {
                        val validator = builder.validators[message.command::class.java.name]
                        if (validator == null) {
                            message.valid.complete(ProcessingResult.NotHandledByProcessor)
                        } else {
                            val resultBuilder = ProcessingResultBuilder(message.command)
                            val validationResult = validator.invoke(resultBuilder)
                            message.valid.complete(validationResult)
                        }
                    } catch (e: Exception) {
                        message.valid.complete(ProcessingResult.Invalid(e))
                    }
                }
            }

            return channel
        }
    }
}