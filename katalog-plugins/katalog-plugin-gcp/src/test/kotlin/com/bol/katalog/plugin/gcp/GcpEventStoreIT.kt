package com.bol.katalog.plugin.gcp

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.time.Instant

@Ignore("Need to set up a new GCP project for testing purposes")
@RunWith(SpringRunner::class)
@SpringBootTest
class GcpEventStoreIT {
    @Autowired
    private lateinit var eventStore: EventStore

    @After
    fun after() {
        (eventStore as GcpEventStore).deleteAll()
    }

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            eventStore.store(PersistentEvent(at(101, "a"), TestEvent("1")))
            eventStore.store(PersistentEvent(at(102, "b"), TestEvent("2")))
            eventStore.store(PersistentEvent(at(103, "c"), TestEvent("3")))

            val (data, cursor) = eventStore.get(EventQuery(pageSize = 2))
            expectThat(data).containsExactly(
                PersistentEvent(at(101, "a"), TestEvent("1")),
                PersistentEvent(at(102, "b"), TestEvent("2"))
            )

            val (data2, _) = eventStore.get(EventQuery(cursor = cursor))
            expectThat(data2).containsExactly(PersistentEvent(at(103, "c"), TestEvent("3")))
        }
    }

    private fun at(millis: Long, username: String) =
        PersistentEvent.Metadata(timestamp = Instant.ofEpochMilli(millis), username = username)

    data class TestEvent(val data: String) : Event()
}
