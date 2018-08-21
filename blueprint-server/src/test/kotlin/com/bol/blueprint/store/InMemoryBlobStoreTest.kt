package com.bol.blueprint.store

import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
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

            assertThat(blobStore.get(URI.create("/foo/bar"))).isEqualTo(bar)
            assertThat(blobStore.get(URI.create("/foo/bar/baz"))).isEqualTo(baz)
        }
    }

    @Test
    fun `Can check if blob exists`() {
        runBlocking {
            blobStore.store(URI.create("/foo/bar"), byteArrayOf(4, 5, 6))

            assertThat(blobStore.exists(URI.create("/foo/bar"))).isTrue()
            assertThat(blobStore.exists(URI.create("/unknown"))).isFalse()
        }
    }
}