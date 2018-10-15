package com.bol.blueprint.plugin.gcp

import com.bol.blueprint.domain.Event
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import strikt.api.expectThat
import strikt.assertions.containsExactly
import java.time.Instant

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
            eventStore.store(Event(at(101, "a"), mapOf("foo" to "1")))
            eventStore.store(Event(at(102, "b"), mapOf("foo" to "2")))
            eventStore.store(Event(at(103, "c"), mapOf("foo" to "3")))

            val (data, cursor) = eventStore.get(EventQuery(pageSize = 2))
            expectThat(data).containsExactly(Event(at(101, "a"), mapOf("foo" to "1")), Event(at(102, "b"), mapOf("foo" to "2")))

            val (data2, _) = eventStore.get(EventQuery(cursor = cursor))
            expectThat(data2).containsExactly(Event(at(103, "c"), mapOf("foo" to "3")))
        }
    }

    private fun at(millis: Long, username: String) = Event.Metadata(timestamp = Instant.ofEpochMilli(millis), username = username)
}
