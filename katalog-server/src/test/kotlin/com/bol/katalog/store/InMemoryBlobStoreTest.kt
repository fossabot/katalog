package com.bol.katalog.store

import kotlinx.coroutines.runBlocking
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue
import java.net.URI

class InMemoryBlobStoreTest {
    private val blobStore: BlobStore = InMemoryBlobStore()

    @Test
    fun `Can roundtrip blobs`() {
        val bar = byteArrayOf(1, 2, 3)
        val baz = byteArrayOf(4, 5, 6)

        runBlocking {
            blobStore.store(URI.create("/foo/bar"), bar)
            blobStore.store(URI.create("/foo/bar/baz"), baz)

            expectThat(blobStore.get(URI.create("/foo/bar"))).isEqualTo(bar)
            expectThat(blobStore.get(URI.create("/foo/bar/baz"))).isEqualTo(baz)
        }
    }

    @Test
    fun `Can check if blob exists`() {
        runBlocking {
            blobStore.store(URI.create("/foo/bar"), byteArrayOf(4, 5, 6))

            expectThat(blobStore.exists(URI.create("/foo/bar"))).isTrue()
            expectThat(blobStore.exists(URI.create("/unknown"))).isFalse()
        }
    }
}