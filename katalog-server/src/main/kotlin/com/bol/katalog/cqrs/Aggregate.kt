package com.bol.katalog.cqrs

interface Aggregate<S : State> {
    suspend fun <T> read(block: suspend S.() -> T): T
    suspend fun send(c: Command)
}