package com.bol.katalog.cqrs.commands

sealed class CommandValidationFailure {
    object Conflict : CommandValidationFailure()
    object NotFound : CommandValidationFailure()
}
