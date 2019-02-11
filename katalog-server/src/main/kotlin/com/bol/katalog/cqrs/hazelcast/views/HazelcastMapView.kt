package com.bol.katalog.cqrs.hazelcast.views

import com.hazelcast.core.BaseMap

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