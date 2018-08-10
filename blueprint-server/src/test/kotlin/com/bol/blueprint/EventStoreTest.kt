package com.bol.blueprint

import com.bol.blueprint.domain.Dispatcher
import com.bol.blueprint.domain.SchemaType
import com.bol.blueprint.domain.UntypedEvent
import com.bol.blueprint.domain.Version
import com.bol.blueprint.queries.Query
import com.bol.blueprint.store.*
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

@Transactional
abstract class AbstractEventStoreTest {
    protected abstract var eventStore: EventStore

    @Test
    fun `Can roundtrip events`() {
        runBlocking {
            eventStore.store(UntypedEvent(mapOf("foo" to "1")))
            eventStore.store(UntypedEvent(mapOf("foo" to "2")))
            eventStore.store(UntypedEvent(mapOf("foo" to "3")))

            val (data, nextPageAfterId) = eventStore.get(EventQuery(pageSize = 2))
            assertThat(data).containsExactly(UntypedEvent(mapOf("foo" to "1")), UntypedEvent(mapOf("foo" to "2")))

            val (data2, _) = eventStore.get(EventQuery(afterId = nextPageAfterId))
            assertThat(data2).containsExactly(UntypedEvent(mapOf("foo" to "3")))
        }
    }

    @Test
    fun `Can replay from store`() {
        val dispatcher = Dispatcher(eventStore, InMemoryBlobStore())

        // Create some events
        runBlocking {
            dispatcher.createNamespace(DomainTest.NS1)
            dispatcher.createSchema(DomainTest.SCHEMA1, SchemaType.default())
            dispatcher.createVersion(DomainTest.VERSION1)
        }

        // Replay them from the store
        val query = Query()
        dispatcher.addListener(query)
        runBlocking {
            dispatcher.replayFromStore()
        }

        // Check the resulting query
        Awaitility.await().untilAsserted {
            assertThat(query.getVersions(DomainTest.SCHEMA1)).containsExactlyInAnyOrder(
                Version("1.0.0")
            )
        }
    }
}

@RunWith(SpringRunner::class)
@SpringBootTest
class PostgresEventStoreTest : AbstractEventStoreTest() {
    override lateinit var eventStore: EventStore

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Before
    fun before() {
        eventStore = PostgresEventStore(jdbcTemplate)
    }
}

class InMemoryEventStoreTest : AbstractEventStoreTest() {
    override lateinit var eventStore: EventStore

    @Before
    fun before() {
        eventStore = InMemoryEventStore()
    }
}