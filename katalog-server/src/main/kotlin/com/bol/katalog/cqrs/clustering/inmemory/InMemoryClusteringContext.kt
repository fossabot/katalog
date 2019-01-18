package com.bol.katalog.cqrs.clustering.inmemory

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
    private var clusteringChannels = ConcurrentHashMap<Any, ClusteringChannel>()
    private val maps = mutableMapOf<String, Map<*, *>>()

    override fun start() {
        clusteringChannels.clear()
        maps.clear()
    }

    override fun stop() {
        runBlocking {
            clusteringChannels.values.map {
                it.stop()
            }.awaitAll()
        }
    }

    override fun getClusteringChannel(owner: Any): ClusteringChannel =
        clusteringChannels.getOrPut(owner) {
            InMemoryClusteringChannel()
        }

    override suspend fun <E : Event> persist(event: E): PersistentEvent<E> {
        val persistentEvent = event.asPersistentEvent(CoroutineUserContext.get(), clock)
        eventStore.store(persistentEvent)
        return persistentEvent
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> getMap(name: String): MutableMap<K, V> = maps.getOrPut(name) {
        mutableMapOf<K, V>()
    } as MutableMap<K, V>
}