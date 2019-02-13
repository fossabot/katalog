package com.bol.katalog.cqrs.hazelcast.primitives

import com.bol.katalog.cqrs.hazelcast.CoroutineTransactionContext
import com.hazelcast.core.BaseMap
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

    /**
     * A read-only Map<K, V> wrapper around Hazelcast's BaseMap
     */
    class HazelcastMapView<K : Any, V : Any>(val map: BaseMap<K, V>) : Map<K, V> {
        override val entries: Set<Map.Entry<K, V>>
            get() = throw NotImplementedError()

        override val keys: Set<K>
            get() = map.keySet()

        override val size: Int
            get() = map.size()

        override val values: Collection<V>
            get() = map.values()

        override fun containsKey(key: K) = map.containsKey(key)

        override fun containsValue(value: V) = throw NotImplementedError()

        override operator fun get(key: K): V? = map.get(key)

        override fun isEmpty() = map.isEmpty
    }
}