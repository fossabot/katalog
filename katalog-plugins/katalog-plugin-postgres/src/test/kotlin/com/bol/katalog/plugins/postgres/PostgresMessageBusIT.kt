package com.bol.katalog.plugins.postgres

import com.bol.katalog.messaging.MessageBus
import com.bol.katalog.testing.messagebus.AbstractMessageBusTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest
@Transactional
class PostgresMessageBusIT : AbstractMessageBusTest() {
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
