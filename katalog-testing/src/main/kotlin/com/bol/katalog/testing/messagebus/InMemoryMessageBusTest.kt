package com.bol.katalog.testing.messagebus

import com.bol.katalog.messaging.inmemory.InMemoryMessageBus
import org.junit.jupiter.api.Test

class InMemoryMessageBusTest : AbstractMessageBusTest() {
    @Test
    fun `Can roundtrip messages`() {
        canRoundtripMessages(InMemoryMessageBus())
    }

    @Test
    fun `Can retry messages`() {
        canRetryMessages(InMemoryMessageBus())
    }
}