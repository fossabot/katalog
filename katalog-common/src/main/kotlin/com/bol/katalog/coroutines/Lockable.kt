package com.bol.katalog.coroutines

interface Lockable {
    suspend fun <T> withReadLock(block: suspend () -> T): T
    suspend fun <T> withWriteLock(block: suspend () -> T): T
}