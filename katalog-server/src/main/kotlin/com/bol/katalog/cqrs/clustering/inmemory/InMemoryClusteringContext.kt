package com.bol.katalog.cqrs.clustering.inmemory

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent
import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.ClusteringContext
import com.bol.katalog.cqrs.clustering.asPersistentEvent
import com.bol.katalog.security.CoroutineUserContext
import com.bol.katalog.store.EventStore
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap

class InMemoryClusteringContext(
    private val eventStore: EventStore,
    private val clock: Clock
) : ClusteringContext {
    private var clusteringChannels = ConcurrentHashMap<Aggregate<*>, ClusteringChannel>()

    override fun start() {
        clusteringChannels.clear()
    }

    override fun stop() {
        runBlocking {
            clusteringChannels.values.map {
                it.stop()
            }.awaitAll()
        }
    }

    override fun getClusteringChannel(aggregate: Aggregate<*>): ClusteringChannel =
        clusteringChannels.getOrPut(aggregate) {
            InMemoryClusteringChannel()
        }

    override suspend fun <E : Event> persist(event: E): PersistentEvent<E> {
        val persistentEvent = event.asPersistentEvent(CoroutineUserContext.get(), clock)
        eventStore.store(persistentEvent)
        return persistentEvent
    }

    override fun <K, V> getMap(name: String): MutableMap<K, V> = mutableMapOf()
}