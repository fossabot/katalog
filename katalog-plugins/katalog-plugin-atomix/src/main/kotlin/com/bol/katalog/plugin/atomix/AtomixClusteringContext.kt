package com.bol.katalog.plugin.atomix

import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.ClusteringContext
import io.atomix.core.Atomix
import io.atomix.primitive.Consistency
import io.atomix.primitive.Replication
import io.atomix.protocols.backup.MultiPrimaryProtocol
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class AtomixClusteringContext(private val atomix: Atomix) : ClusteringContext {
    private var clusteringChannels = ConcurrentHashMap<Any, ClusteringChannel>()
    private val maps = ConcurrentHashMap<String, Map<*, *>>()

    override fun getClusteringChannel(owner: Any): ClusteringChannel {
        return clusteringChannels.getOrPut(owner) {
            AtomixClusteringChannel(atomix)
        }
    }

    override fun <K, V> getMap(name: String): MutableMap<K, V> {
        @Suppress("UNCHECKED_CAST")
        val map = maps.getOrPut(name) {
            val protocol = MultiPrimaryProtocol.builder()
                .withConsistency(Consistency.LINEARIZABLE)
                .withReplication(Replication.SYNCHRONOUS)
                .build()

            val map: Map<K, V> = atomix.mapBuilder<K, V>(name)
                .withProtocol(protocol)
                .build()

            map
        } as Map<K, V>

        return map.toMutableMap()
    }

    override fun ifRequiresReplay(block: suspend () -> Unit) {
        // Are we the first active node? Then we're considered the leader
        if (atomix.membershipService.members.firstOrNull() == atomix.membershipService.localMember) {
            runBlocking {
                block()
            }
        }
    }

    override fun start() {
    }

    override fun stop() {
        runBlocking {
            clusteringChannels.values.map {
                it.stop()
            }.awaitAll()
        }
    }
}