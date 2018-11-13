package com.bol.blueprint.cqrs.commands

import com.bol.blueprint.domain.Command
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import org.springframework.stereotype.Component

@Component
class CommandValidator(
    private val handlers: List<CommandHandler>
) {
    suspend fun validate(command: Command) {
        val completions = handlers
            .map {
                val msg = CommandHandler.UnvalidatedCommand(
                    command = command,
                    valid = CompletableDeferred()
                )

                it.commandHandler.send(msg)

                msg.valid
            }

        // Fail the validation if any of the completed validation results is *present* and *false*
        val failures = completions.awaitAll().filterNotNull()

        if (failures.isNotEmpty()) {
            throw InvalidCommandException(failures)
        }
    }
}