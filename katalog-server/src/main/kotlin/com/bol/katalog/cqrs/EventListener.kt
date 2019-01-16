package com.bol.katalog.cqrs

interface EventListener {
    suspend fun beforeEventProcessed(event: Event): PersistentEvent.Metadata
}