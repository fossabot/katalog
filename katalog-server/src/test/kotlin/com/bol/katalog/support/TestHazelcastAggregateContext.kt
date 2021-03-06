package com.bol.katalog.support

import com.bol.katalog.config.HazelcastAutoConfiguration
import com.bol.katalog.config.HazelcastProperties
import com.bol.katalog.cqrs.AbstractAggregate
import com.bol.katalog.cqrs.AggregateContext
import com.bol.katalog.cqrs.Event
import com.bol.katalog.store.EventStore
import com.bol.katalog.store.inmemory.InMemoryEventStore
import com.bol.katalog.testing.TestData
import com.hazelcast.core.HazelcastInstance
import java.time.Clock

class TestHazelcastAggregateContext(
    eventStore: EventStore,
    clock: Clock
) : AggregateContext(getTestHazelcast(), eventStore, clock), AutoCloseable {

    var onEvent: ((Event) -> Unit)? = null

    override suspend fun <E : Event> publish(event: E) {
        onEvent?.invoke(event)
        super.publish(event)
    }

    override fun close() {
        hazelcast.shutdown()
        hazelcastInstance = null
    }

    inline fun <reified T : AbstractAggregate> get(): T {
        return getRegisteredAggregates().single { it.javaClass == T::class.java } as T
    }

    companion object {
        private var hazelcastInstance: HazelcastInstance? = null

        fun get() = TestHazelcastAggregateContext(InMemoryEventStore(), TestData.clock)

        fun getTestHazelcast(): HazelcastInstance {
            if (hazelcastInstance == null) {
                hazelcastInstance = HazelcastAutoConfiguration().standaloneHazelcastInstance(HazelcastProperties())
            }

            return hazelcastInstance!!
        }
    }
}