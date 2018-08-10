package com.bol.blueprint.plugins.postgres

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

@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class PostgresEventStoreTest {
    @Autowired
    private lateinit var eventStore: EventStore

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            eventStore.store(UntypedEvent(mapOf("foo" to "1")))
            eventStore.store(UntypedEvent(mapOf("foo" to "2")))
            eventStore.store(UntypedEvent(mapOf("foo" to "3")))

            val (data, nextPageAfterId) = eventStore.get(EventQuery(pageSize = 2))
            Assertions.assertThat(data).containsExactly(UntypedEvent(mapOf("foo" to "1")), UntypedEvent(mapOf("foo" to "2")))

            val (data2, _) = eventStore.get(EventQuery(afterId = nextPageAfterId))
            Assertions.assertThat(data2).containsExactly(UntypedEvent(mapOf("foo" to "3")))
        }
    }
}
