package com.bol.katalog.cqrs

class CommandFailedException(val failure: CommandResult) : RuntimeException()