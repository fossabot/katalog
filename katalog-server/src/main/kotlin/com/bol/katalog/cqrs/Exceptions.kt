package com.bol.katalog.cqrs

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException(message: String) : RuntimeException(message)

sealed class CommandFailure(message: String) : Command.Result.Failure(message) {
    class NotFound(message: String) : CommandFailure(message)
    class Conflict(message: String) : CommandFailure(message)
    class Forbidden(message: String) : CommandFailure(message)
    class UnknownThrowable(val throwable: Throwable, message: String) : CommandFailure(message)
}

fun Throwable.asCommandFailure(): Command.Result.Failure {
    return when (this) {
        is NotFoundException -> CommandFailure.NotFound(message!!)
        is ConflictException -> CommandFailure.Conflict(message!!)
        is ForbiddenException -> CommandFailure.Forbidden(message!!)
        else -> CommandFailure.UnknownThrowable(this, this.message ?: "No message")
    }
}

fun Command.Result.Failure.asThrowable(): Throwable {
    return when (this) {
        is CommandFailure.NotFound -> NotFoundException(message)
        is CommandFailure.Conflict -> ConflictException(message)
        is CommandFailure.Forbidden -> ForbiddenException(message)
        is CommandFailure.UnknownThrowable -> throwable
        else -> RuntimeException("Unknown command failure: $this")
    }
}