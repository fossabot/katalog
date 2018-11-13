package com.bol.blueprint.cqrs.commands

sealed class CommandValidationFailure {
    data class Conflict(val description: String) : CommandValidationFailure()
    data class UnknownProblem(val description: String) : CommandValidationFailure()
}
