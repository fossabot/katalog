package com.bol.katalog.cqrs

import com.bol.katalog.users.UserId

interface Command {
    data class Metadata(val userId: UserId)

    sealed class Result {
        object Success : Result()
        open class Failure(val message: String) : Result()
    }
}