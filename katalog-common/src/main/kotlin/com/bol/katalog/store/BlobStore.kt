package com.bol.katalog.store

import java.net.URI

interface BlobStore {
    suspend fun delete(path: URI)
    suspend fun exists(path: URI): Boolean
    suspend fun get(path: URI): ByteArray?
    suspend fun store(path: URI, data: ByteArray)

    suspend fun deleteIfExists(path: URI) {
        if (exists(path)) {
            delete(path)
        }
    }
}