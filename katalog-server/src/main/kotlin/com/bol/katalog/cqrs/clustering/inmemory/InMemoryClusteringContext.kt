package com.bol.katalog.cqrs.clustering.inmemory

import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.ClusteringContext
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class InMemoryClusteringContext : ClusteringContext {
    private var clusteringChannels = ConcurrentHashMap<Any, ClusteringChannel>()
    private val maps = ConcurrentHashMap<String, Map<*, *>>()

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

    override fun ifRequiresReplay(block: suspend () -> Unit) {
        runBlocking {
            block()
        }
    }

    override fun getClusteringChannel(owner: Any): ClusteringChannel =
        clusteringChannels.getOrPut(owner) {
            InMemoryClusteringChannel()
        }

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> getMap(name: String): MutableMap<K, V> = maps.getOrPut(name) {
        mutableMapOf<K, V>()
    } as MutableMap<K, V>
}