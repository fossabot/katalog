package com.bol.katalog.plugin.gcp

import com.bol.katalog.store.EventStore
import com.bol.katalog.testing.store.AbstractEventStoreTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@Disabled("Need to set up a new GCP project for testing purposes")
@ExtendWith(SpringExtension::class)
@SpringBootTest
class GcpEventStoreIT : AbstractEventStoreTest() {
    @Autowired
    private lateinit var eventStore: EventStore

    @AfterEach
    fun after() {
        (eventStore as GcpEventStore).deleteAll()
    }

    @Test
    fun `Can roundtrip events`() {
        canRoundtripEvents(eventStore)
    }

    @Test
    fun `Can page events`() {
        canPageEvents(eventStore)
    }
}
