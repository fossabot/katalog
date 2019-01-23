package com.bol.katalog.cqrs

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(message: String? = null) : RuntimeException(message)

data class NotFoundFailure(val message: String? = null) : Command.Failure

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(message: String? = null) : RuntimeException(message)

data class ConflictFailure(val message: String? = null) : Command.Failure
