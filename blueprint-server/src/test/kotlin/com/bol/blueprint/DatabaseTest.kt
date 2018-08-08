package com.bol.blueprint

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

@RunWith(SpringRunner::class)
@SpringBootTest
@Transactional
class DatabaseTest {
    @Autowired
    private lateinit var eventStore: PostgresEventStore

    @Test
    fun `Can roundtrip events`() {
        eventStore.store(TestEvent("1"))
        eventStore.store(TestEvent("2"))
        eventStore.store(TestEvent("3"))

        val (data, nextPageAfterId) = eventStore.get(EventQuery(pageSize = 2))
        assertThat(data).containsExactly(TestEvent("1"), TestEvent("2"))

        val (data2, _) = eventStore.get(EventQuery(afterId = nextPageAfterId))
        assertThat(data2).containsExactly(TestEvent("3"))
    }

    data class TestEvent(val foo: String) : EventBase
}