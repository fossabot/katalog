package com.bol.blueprint.api.v1

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException : RuntimeException()

@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException : RuntimeException()