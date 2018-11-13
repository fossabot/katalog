package com.bol.blueprint.cqrs.commands

import com.bol.blueprint.domain.Command
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

interface CommandHandler {
    val commandHandler: CommandHandlerChannel

    data class UnvalidatedCommand<T : Command>(
        val command: T,
        val valid: CompletableDeferred<CommandValidationFailure?>
    )
}

typealias CommandHandlerChannel = SendChannel<CommandHandler.UnvalidatedCommand<Command>>