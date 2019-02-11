package com.bol.katalog.cqrs.hazelcast.views

import com.hazelcast.core.BaseMultiMap

/**
 * A read-only wrapper around Hazelcast's BaseMultiMap
 */
class HazelcastMultiMapView<K : Any, V : Any>(val map: BaseMultiMap<K, V>) {
    val size: Int
        get() = map.size()

    operator fun get(key: K): Collection<V>? = map.get(key)
}