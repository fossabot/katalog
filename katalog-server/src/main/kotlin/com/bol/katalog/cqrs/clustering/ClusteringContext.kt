package com.bol.katalog.cqrs.clustering

import com.bol.katalog.cqrs.Aggregate
import com.bol.katalog.cqrs.Event
import com.bol.katalog.cqrs.PersistentEvent

interface ClusteringContext {
    fun getClusteringChannel(aggregate: Aggregate<*>): ClusteringChannel

    suspend fun <E : Event> persist(event: E): PersistentEvent<E>

    fun <K, V> getMap(name: String): MutableMap<K, V>

    fun start()
    fun stop()
}