package com.bol.katalog.coroutines

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MutexLockable : Lockable {
    private val mutex = Mutex()

    override suspend fun <T> withReadLock(block: suspend () -> T): T {
        return mutex.withLock { block() }
    }

    override suspend fun <T> withWriteLock(block: suspend () -> T): T {
        return mutex.withLock { block() }
    }
}