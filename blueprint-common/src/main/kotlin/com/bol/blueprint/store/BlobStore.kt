package com.bol.blueprint.store

import com.bol.blueprint.domain.ArtifactKey
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

fun ArtifactKey.getBlobStorePath(): URI = URI.create("$namespace/$schema/$version/$filename")
