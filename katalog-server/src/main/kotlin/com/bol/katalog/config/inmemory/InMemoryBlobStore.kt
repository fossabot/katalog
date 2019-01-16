package com.bol.katalog.config.inmemory

import com.bol.katalog.store.BlobStore
import java.net.URI

class InMemoryBlobStore : BlobStore {
    private val store = mutableMapOf<URI, ByteArray>()

    override suspend fun delete(path: URI) {
        store.remove(path)
    }

    override suspend fun exists(path: URI): Boolean = store.containsKey(path)

    override suspend fun get(path: URI): ByteArray? = store[path]

    override suspend fun store(path: URI, data: ByteArray) {
        store[path] = data
    }

    fun reset() {
        store.clear()
    }
}