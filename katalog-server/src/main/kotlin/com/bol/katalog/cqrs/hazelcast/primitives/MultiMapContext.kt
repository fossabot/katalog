package com.bol.katalog.cqrs.hazelcast.primitives

import com.bol.katalog.cqrs.hazelcast.CoroutineTransactionContext
import com.hazelcast.core.BaseMultiMap
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.TransactionalMultiMap

class MultiMapContext<K : Any, V : Any>(
    private val hazelcast: HazelcastInstance,
    private val name: String
) {
    suspend fun <T> read(block: HazelcastMultiMapView<K, V>.() -> T): T {
        val tx = CoroutineTransactionContext.get()
        val map = tx?.hazelcastTx()?.getMultiMap<K, V>(name) ?: hazelcast.getMultiMap<K, V>(name)
        return block(HazelcastMultiMapView(map))
    }

    suspend fun <T> write(block: TransactionalMultiMap<K, V>.() -> T): T {
        val tx = CoroutineTransactionContext.get() ?: throw IllegalStateException("No active transaction")
        return block(tx.hazelcastTx().getMultiMap(name))
    }

    fun reset() {
        hazelcast.getMultiMap<K, V>(name).destroy()
    }

    /**
     * A read-only wrapper around Hazelcast's BaseMultiMap
     */
    class HazelcastMultiMapView<K : Any, V : Any>(val map: BaseMultiMap<K, V>) {
        val size: Int
            get() = map.size()

        operator fun get(key: K): Collection<V>? = map.get(key)
    }
}