package com.bol.katalog.testing.store

import com.bol.katalog.store.BlobStore
import kotlinx.coroutines.runBlocking
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.net.URI

abstract class AbstractBlobStoreTest {
    fun canRoundtripBlobs(blobStore: BlobStore) {
        val bar = byteArrayOf(1, 2, 3)
        val baz = byteArrayOf(4, 5, 6)

        runBlocking {
            blobStore.store(URI.create("/foo/bar"), bar)
            blobStore.store(URI.create("/foo/bar/baz"), baz)

            expectThat(blobStore.get(URI.create("/foo/bar"))).isEqualTo(bar)
            expectThat(blobStore.get(URI.create("/foo/bar/baz"))).isEqualTo(baz)
        }
    }

    fun canCheckIfBlobExists(blobStore: BlobStore) {
        runBlocking {
            blobStore.store(URI.create("/foo/bar"), byteArrayOf(4, 5, 6))

            expectThat(blobStore.exists(URI.create("/foo/bar"))).isTrue()
            expectThat(blobStore.exists(URI.create("/unknown"))).isFalse()
        }
    }
}