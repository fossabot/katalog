package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent

interface ClusteringContext {
    fun getClusteringChannel(owner: Any): ClusteringChannel

    suspend fun <E : Event> persist(event: E): PersistentEvent<E>

    fun <K, V> getMap(name: String): MutableMap<K, V>

    fun ifRequiresReplay(block: suspend () -> Unit)

    fun start()
    fun stop()
}