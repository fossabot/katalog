package com.bol.katalog.testing.store

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.store.EventQuery
import com.bol.katalog.store.EventStore
import com.bol.katalog.users.UserId
import kotlinx.coroutines.runBlocking
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.time.Instant

abstract class AbstractEventStoreTest {
    fun canRoundtripEvents(eventStore: EventStore) {
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

    fun canPageEvents(eventStore: EventStore) {
        runBlocking {
            (1..50).forEach {
                eventStore.store(PersistentEvent(at(101 + it.toLong(), "a"), TestEvent(it.toString())))
            }

            var result = eventStore.get(EventQuery(pageSize = 24))
            expectThat((result.data.first().data as TestEvent).data).isEqualTo("1")
            expectThat((result.data.last().data as TestEvent).data).isEqualTo("24")

            result = eventStore.get(result.toNextPageQuery(24))
            expectThat((result.data.first().data as TestEvent).data).isEqualTo("25")
            expectThat((result.data.last().data as TestEvent).data).isEqualTo("48")

            result = eventStore.get(result.toNextPageQuery(24))
            expectThat((result.data.first().data as TestEvent).data).isEqualTo("49")
            expectThat((result.data.last().data as TestEvent).data).isEqualTo("50")
            expectThat(result.nextPageCursor).isNull()
        }
    }

    private fun at(millis: Long, userId: UserId) =
        PersistentEvent.Metadata(timestamp = Instant.ofEpochMilli(millis), userId = userId)

    data class TestEvent(val data: String) : Event
}