package com.bol.katalog.plugin.hazelcast

import java.util.concurrent.LinkedBlockingDeque

class Pool<T : Any>(maxSize: Int, construct: () -> T) {
    val pool = LinkedBlockingDeque<T>(maxSize)

    init {
        repeat(maxSize) {
            pool.push(construct())
        }
    }

    fun getAll() = pool.toList()

    fun <R> lease(block: (T) -> R): R {
        val item = pool.takeFirst()
        val result: R = block(item)
        pool.putLast(item)
        return result
    }
}