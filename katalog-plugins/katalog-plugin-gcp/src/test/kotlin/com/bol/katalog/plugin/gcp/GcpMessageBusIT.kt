package com.bol.katalog.plugin.gcp

import com.bol.katalog.messaging.MessageBus
import com.bol.katalog.testing.messagebus.AbstractMessageBusTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@Disabled("Need to set up a new GCP project for testing purposes")
@ExtendWith(SpringExtension::class)
@SpringBootTest
class GcpMessageBusIT : AbstractMessageBusTest() {
    @Autowired
    private lateinit var bus: MessageBus

    @Test
    fun `Can roundtrip messages`() {
        canRoundtripMessages(bus)
    }

    @Test
    fun `Can retry messages`() {
        canRetryMessages(bus)
    }
}
