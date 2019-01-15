package com.bol.katalog.cqrs

interface CommandProcessor {
    suspend fun <TCommand : Command> apply(command: TCommand)
}