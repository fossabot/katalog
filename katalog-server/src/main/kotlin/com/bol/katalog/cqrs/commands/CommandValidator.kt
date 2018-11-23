package com.bol.katalog.cqrs.commands

import com.bol.katalog.domain.Command
import com.bol.katalog.domain.ConflictException
import com.bol.katalog.domain.NotFoundException
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
        val failure = completions.awaitAll().filterNotNull().singleOrNull()

        if (failure != null) {
            when (failure) {
                CommandValidationFailure.Conflict -> throw ConflictException()
                CommandValidationFailure.NotFound -> throw NotFoundException()
            }
        }
    }
}