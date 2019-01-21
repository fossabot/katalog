package com.bol.katalog.cqrs.clustering

interface ClusteringContext {
    fun getClusteringChannel(owner: Any): ClusteringChannel

    fun <K, V> getMap(name: String): MutableMap<K, V>

    fun ifRequiresReplay(block: suspend () -> Unit)

    fun start()
    fun stop()
}