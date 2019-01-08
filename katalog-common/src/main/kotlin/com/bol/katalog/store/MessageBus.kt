package com.bol.katalog.store

interface MessageBus {
    suspend fun publish(queue: String, task: Any)
    suspend fun receive(queue: String, handler: suspend (Any) -> Unit)
}
