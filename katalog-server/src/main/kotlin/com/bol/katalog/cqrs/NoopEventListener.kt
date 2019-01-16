package com.bol.katalog.cqrs

import java.time.Instant

class NoopEventListener : EventListener {
    override suspend fun beforeEventProcessed(event: Event): PersistentEvent.Metadata {
        return PersistentEvent.Metadata(Instant.now(), "unknown")
    }
}