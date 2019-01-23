package com.bol.katalog.cqrs

interface Command {
    interface Result

    object Success : Result
    interface Failure : Result
    data class UnknownFailure(val message: String? = null) : Failure
}
