package com.bol.katalog.store

interface TaskStore {
    suspend fun publish(queue: String, task: Any)
    suspend fun receive(queue: String, handler: (Any) -> Unit): Boolean
}
