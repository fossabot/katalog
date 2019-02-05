package com.bol.katalog.cqrs

import com.bol.katalog.users.UserId

interface Aggregate<S : State> {
    suspend fun <T> readAs(userId: UserId, block: suspend S.() -> T): T
    suspend fun sendAs(userId: UserId, c: Command)
    fun directAccess(): DirectAggregate
}