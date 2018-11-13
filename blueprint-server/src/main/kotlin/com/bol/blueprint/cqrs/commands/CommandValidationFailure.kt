package com.bol.blueprint.cqrs.commands

sealed class CommandValidationFailure {
    object Conflict : CommandValidationFailure()
    object NotFound : CommandValidationFailure()
}
