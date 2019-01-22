package com.bol.katalog.cqrs.clustering.inmemory

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.Command
import com.bol.katalog.cqrs.clustering.ClusteringChannel
import com.bol.katalog.cqrs.clustering.ClusteringContext
import com.bol.katalog.cqrs.clustering.IAggregate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class InMemoryClusteringContext(private val aggregates: List<IAggregate>) : ClusteringContext {
    private var queues = mutableMapOf<IAggregate, InMemoryQueue>()
    private val maps = ConcurrentHashMap<String, Map<*, *>>()

    init {
        start()
    }

    private fun start() {
        queues = mutableMapOf()
        aggregates.forEach {
            queues[it] = InMemoryQueue(it)
        }

        maps.clear()
    }

    override fun close() {
        runBlocking {
            queues.values.forEach { it.close() }
        }
    }

    override suspend fun <C : Command> send(aggregate: IAggregate, command: C): Deferred<Unit> {
        return queues[aggregate]?.send(command) ?: throw IllegalStateException("Aggregate has no queue: $aggregate")
    }

    override fun ifRequiresReplay(block: suspend () -> Unit) {
        runBlocking {
            block()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> getMap(name: String): MutableMap<K, V> = maps.getOrPut(name) {
        mutableMapOf<K, V>()
    } as MutableMap<K, V>
}