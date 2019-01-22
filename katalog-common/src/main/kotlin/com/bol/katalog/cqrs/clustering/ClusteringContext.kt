package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Command
import kotlinx.coroutines.Deferred

interface ClusteringContext {
    fun <K, V> getMap(name: String): MutableMap<K, V>

    suspend fun <C: Command> send(aggregate: IAggregate, command: C): Deferred<Unit>

    fun ifRequiresReplay(block: suspend () -> Unit)

    fun close()
}