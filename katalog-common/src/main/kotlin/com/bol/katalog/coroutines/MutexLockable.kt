package com.bol.katalog.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MutexLockable : Lockable {
    private val mutex = Mutex()

    /**
     * When reading we assume that the mutex may already be locked by the writer. If so, we can simply run the block.
     */
    override suspend fun <T> withReadLock(block: suspend () -> T): T {
        return if (mutex.isLocked) {
            block()
        } else mutex.withLock { block() }
    }

    override suspend fun <T> withWriteLock(block: suspend () -> T): T {
        return mutex.withLock { block() }
    }
}