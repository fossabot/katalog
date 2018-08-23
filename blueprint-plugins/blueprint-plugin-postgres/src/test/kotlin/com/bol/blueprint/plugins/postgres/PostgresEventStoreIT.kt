package com.bol.blueprint.plugins.postgres

import com.bol.blueprint.domain.Event
import com.bol.blueprint.domain.Event.Companion.event
import com.bol.blueprint.domain.UntypedEvent
import com.bol.blueprint.store.EventQuery
import com.bol.blueprint.store.EventStore
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class PostgresEventStoreIT {
    @Autowired
    private lateinit var eventStore: EventStore

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            val metadata = Event.Metadata(timestamp = Instant.ofEpochMilli(123))
            eventStore.store(event(metadata) { UntypedEvent(mapOf("foo" to "1")) })
            eventStore.store(event(metadata) { UntypedEvent(mapOf("foo" to "2")) })
            eventStore.store(event(metadata) { UntypedEvent(mapOf("foo" to "3")) })

            val (data, cursor) = eventStore.get(EventQuery(pageSize = 2))
            Assertions.assertThat(data).containsExactly(event(metadata) { UntypedEvent(mapOf("foo" to "1")) }, event(metadata) { UntypedEvent(mapOf("foo" to "2")) })

            val (data2, _) = eventStore.get(EventQuery(cursor = cursor))
            Assertions.assertThat(data2).containsExactly(event(metadata) { UntypedEvent(mapOf("foo" to "3")) })
        }
    }
}
