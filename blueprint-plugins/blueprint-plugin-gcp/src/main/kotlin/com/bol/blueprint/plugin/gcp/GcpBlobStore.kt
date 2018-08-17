package com.bol.blueprint.plugin.gcp

import com.bol.blueprint.store.BlobStore
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import java.net.URI
import java.nio.channels.Channels

class GcpBlobStore(private val storage: Storage, private val gcpProperties: GcpProperties) : BlobStore {
    override suspend fun delete(path: URI) {
        storage.delete(getBlobId(path))
    }

    override suspend fun exists(path: URI) = storage.get(getBlobId(path)) != null

    override suspend fun get(path: URI) = storage.get(getBlobId(path)).let {
        it.reader().use { reader ->
            Channels.newInputStream(reader).readBytes()
        }
    }

    override suspend fun store(path: URI, data: ByteArray) {
        storage.create(BlobInfo.newBuilder(gcpProperties.bucketName, path.toString()).build()).let {
            it.writer().use { writer ->
                Channels.newOutputStream(writer).write(data)
            }
        }
    }

    private fun getBlobId(path: URI) = BlobId.of(gcpProperties.bucketName, path.toString())
}