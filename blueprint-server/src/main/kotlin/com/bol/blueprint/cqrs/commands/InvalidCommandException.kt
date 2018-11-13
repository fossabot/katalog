package com.bol.blueprint.cqrs.commands

data class InvalidCommandException(val failures: List<CommandValidationFailure>) : RuntimeException()