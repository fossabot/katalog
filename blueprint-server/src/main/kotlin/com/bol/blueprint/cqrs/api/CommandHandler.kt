package com.bol.blueprint.cqrs.api

import com.bol.blueprint.domain.Command
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel

interface CommandHandler {
    val commandHandler: SendChannel<UnvalidatedCommand<Command>>

    data class UnvalidatedCommand<T : Command>(
        val command: T,
        val valid: CompletableDeferred<Boolean?>
    )
}