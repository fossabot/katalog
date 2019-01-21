package com.bol.katalog.cqrs

interface EventPersister {
    suspend fun <E : Event> persist(event: E): PersistentEvent<E>
}