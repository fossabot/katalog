package com.bol.katalog.testing.store

import com.bol.katalog.store.inmemory.InMemoryEventStore
import org.junit.jupiter.api.Test

class InMemoryEventStoreTest : AbstractEventStoreTest() {
    @Test
    fun `Can roundtrip events`() {
        canRoundtripEvents(InMemoryEventStore())
    }
}