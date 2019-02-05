package com.bol.katalog.plugin.hazelcast

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import java.util.concurrent.locks.StampedLock
import kotlin.concurrent.thread

class PoolTest {
    @Test
    fun `Can access concurrently`() {
        val pool = Pool(5) { LockedCounter() }
        val threads = (1..10000).map {
            thread {
                pool.lease {
                    it.increment()
                }
            }
        }
        threads.forEach { it.join() }

        val total = pool.getAll().sumBy { it.value }
        expectThat(total).isEqualTo(10000)
    }

    class LockedCounter {
        var value = 0
        var lock = StampedLock()

        fun increment() {
            expectThat(lock.isWriteLocked).isFalse()
            val stamp = lock.writeLock()
            value++
            lock.unlockWrite(stamp)
        }
    }
}