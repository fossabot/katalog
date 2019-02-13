package com.bol.katalog.cqrs.hazelcast.views

import com.bol.katalog.cqrs.hazelcast.CoroutineTransactionContext
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.TransactionalMap

class MapContext<K : Any, V : Any>(
    private val hazelcast: HazelcastInstance,
    private val name: String
) {
    suspend fun <T> read(block: HazelcastMapView<K, V>.() -> T): T {
        val tx = CoroutineTransactionContext.get()
        val map = tx?.hazelcastTx()?.getMap<K, V>(name) ?: hazelcast.getMap<K, V>(name)
        return block(HazelcastMapView(map))
    }

    suspend fun <T> write(block: TransactionalMap<K, V>.() -> T): T {
        val tx = CoroutineTransactionContext.get() ?: throw IllegalStateException("No active transaction")
        return block(tx.hazelcastTx().getMap(name))
    }

    fun reset() {
        hazelcast.getMap<K, V>(name).destroy()
    }
}