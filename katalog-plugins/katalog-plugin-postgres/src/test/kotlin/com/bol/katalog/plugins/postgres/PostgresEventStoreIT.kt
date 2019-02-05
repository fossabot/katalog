package com.bol.katalog.plugins.postgres

import com.bol.katalog.store.EventStore
import com.bol.katalog.testing.store.AbstractEventStoreTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest
@Transactional
class PostgresEventStoreIT : AbstractEventStoreTest() {
    @Autowired
    private lateinit var eventStore: EventStore

    @Test
    fun `Can roundtrip events`() {
        canRoundtripEvents(eventStore)
    }

    @Test
    fun `Can page events`() {
        canPageEvents(eventStore)
    }
}
